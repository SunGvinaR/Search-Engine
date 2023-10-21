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

    public SearchResult getResult(String query, String site, Integer offset, Integer limit) {
        SearchResult searchResult = new SearchResult();
        if (offset == null){
            offset = 0;
        }
        if(limit == null){
            limit = 20;
        }
        try {
            LemmaFinder lemmaFinder = LemmaFinder.getInstance();
            Set<String> lemmaSet = lemmaFinder.getLemmaSet(query);
            ArrayList<Lemma> lemmas = connectionSQL.searchLemmas(site, lemmaSet);
            ArrayList<Page> pages = searchPages(lemmas);

            ArrayList<FindPage> findPages = new ArrayList<>();
            for (Page page : pages) {
                FindPage findPage = new FindPage();
                findPage.setRelevance(absRelevance(page, lemmas));
                findPage.setUri(page.getPath());
                findPage.setSite(page.getSite().getUrl());
                findPages.add(findPage);
            }
            findPages.sort(Comparator.comparing(FindPage::getRelevance).reversed());
            float maxRelevance = findPages.get(0).getRelevance();
            ArrayList<FindPage> findPagesLimit = new ArrayList<>();
            for (int i = offset; i < limit + offset && i < findPages.size(); i++) {
                findPagesLimit.add(findPages.get(i));
            }
            for (FindPage findPage : findPagesLimit) {
                Page page = connectionSQL.findPageByPathAndSite(findPage.getSite(), findPage.getUri());
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
        return searchResult;
    }

    private ArrayList<Page> searchPages(ArrayList<Lemma> lemmas){
        List<Index> indices = connectionSQL.findIndexesByLemma(lemmas.get(0));
        ArrayList<Page> pages = new ArrayList<>();
        indices.forEach(i -> pages.add(i.getPage()));
        for (int i = 1; i < lemmas.size(); i++) {
            System.out.println(lemmas.get(i).getLemma());
            List<Page> pagesForDelete = new ArrayList<>();
            for (Page page : pages) {
                if (!connectionSQL.existsIndexesByLemmaAndPage(lemmas.get(i), page)) {
                    pagesForDelete.add(page);
                }
            }
            pages.removeAll(pagesForDelete);
            if (pages.isEmpty()) {
                throw new NullPointerException();
            }
        }
        return pages;
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
        ArrayList<Index> indexesOnPage = connectionSQL.findIndexesByLemmasAndPage(lemmas, page);
        float[] absRelevance = {0};
        indexesOnPage.forEach(i -> absRelevance[0] += i.getRank());
        return absRelevance[0];
    }
}
