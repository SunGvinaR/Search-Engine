package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.search.FindPage;
import searchengine.dto.search.SearchResult;
import searchengine.model.*;
import searchengine.services.ConnectionSQL;
import searchengine.services.indexing.LemmaFinder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final ConnectionSQL connectionSQL;

    public SearchResult getResult(String query, String site, int offset, int limit) {
        SearchResult searchResult = new SearchResult();
        System.out.println(query);
        try {
            LemmaFinder lemmaFinder = LemmaFinder.getInstance();
            Set<String> lemmaSet = lemmaFinder.getLemmaSet(query);
            Site site1 = connectionSQL.getSiteRepository().findByUrl(site).get(0);
            ArrayList<Lemma> lemmas = connectionSQL.getLemmaRepository().findByLemmaInAndSiteOrderByFrequencyAsc(lemmaSet, site1);

            List<Index> indices = connectionSQL.getIndexRepository().findByLemma(lemmas.get(0));
            ArrayList<Page> pages = new ArrayList<>();
            indices.forEach(i -> pages.add(i.getPage()));

            for (int i = 1; i < lemmas.size(); i++) {
                System.out.println(lemmas.get(i).getLemma());
                List<Page> pagesForDelete = new ArrayList<>();
                for (Page page : pages) {
                    if (!connectionSQL.getIndexRepository().existsByLemmaAndPage(lemmas.get(i), page)) {
                        pagesForDelete.add(page);
                    }
                }
                pages.removeAll(pagesForDelete);
                if (pages.isEmpty()) {
                    throw new NullPointerException();
                }
            }

            ArrayList<FindPage> findPages = new ArrayList<>();
            for(Page page : pages){
                FindPage findPage = new FindPage();
                findPage.setRelevance(absRelevance(page, lemmas));
                findPage.setUri(page.getPath());
                findPages.add(findPage);
            }
            findPages.sort(Comparator.comparing(FindPage::getRelevance).reversed());
            float maxRelevance = findPages.get(0).getRelevance();
            ArrayList<FindPage> findPagesLimit = new ArrayList<>();
            for (int i = offset; i < limit + offset && i < findPages.size(); i++) {
                findPagesLimit.add(findPages.get(i));
            }

            for (FindPage findPage : findPagesLimit) {
                Page page = connectionSQL.getPageRepository().findByPathAndSite(findPage.getUri(), site1);
                findPage.setSite(page.getSite().getUrl());
                findPage.setSiteName(page.getSite().getName());
                findPage.setRelevance(findPage.getRelevance() / maxRelevance);
                findPage.setTitle(getTitle(page));
                findPage.setSnippet(getSnippet(page, query));
            }
            searchResult.setData(findPagesLimit);
            searchResult.setResult(true);
            searchResult.setCount(pages.size());
        } catch (NullPointerException e) {
            searchResult.setResult(false);
            searchResult.setError("Страницы не найдены");
        } catch (Exception e) {
            searchResult.setResult(false);
            searchResult.setError(e.getMessage());
            e.printStackTrace();
        }
        System.out.println("stop");
        return searchResult;
    }

    private String getSnippet(Page page, String query){
        String content = page.getContent();
        String[] splitQuery = query.split("\s");
        String result = "";

        for (String value : splitQuery) {
            Pattern pattern = Pattern.compile(" .{0,100}" + value + ".{0,100}[А-Яа-яA-za-z0-9]+");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                result = matcher.group();
                break;
            }
        }
        for (String s : splitQuery) {
            result = result.replaceAll(s.toLowerCase(), "<b>" + s + "</b>").trim().concat("...");
        }
        System.out.println(result);
        return result;
    }
    private String getTitle(Page page){
        Pattern pattern = Pattern.compile(".+^\s");
        Matcher matcher = pattern.matcher(page.getContent());
        String title = "";
            while (matcher.find()){
                title = matcher.group();
            }
        return title;
    }
    private float absRelevance(Page page, ArrayList<Lemma> lemmas) {
        ArrayList<Index> indexesOnPage = connectionSQL.getIndexRepository().findByLemmaInAndPage(lemmas, page);
        float[] absRelevance = {0};
        indexesOnPage.forEach(i -> absRelevance[0] += i.getRank());
        return absRelevance[0];
    }
}
