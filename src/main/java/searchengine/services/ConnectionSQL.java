package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@Getter
public class ConnectionSQL {
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    public boolean isPageNotFound(String url, Site site){
        return pageRepository.existsBySiteAndPath(site, url);
    }
    public void deleterSites(Site site){
        List<Site> sitesForDelete = siteRepository.findByUrl(site.getUrl());
        sitesForDelete.forEach(this::deleterPagesOnSite);
        sitesForDelete.forEach(s -> lemmaRepository.deleteAll(lemmaRepository.findBySite(s)));
        siteRepository.deleteAll(sitesForDelete);
    }
    public void deleterPagesOnSite(Site site){
        List<Page> pagesForDelete = pageRepository.findBySite(site);
        pagesForDelete.forEach(this::deleterIndexOnSite);
        pageRepository.deleteAll(pagesForDelete);
    }
    public void deleterPage(Page page){
        deleterIndexOnPage(page);
        pageRepository.delete(page);
    }
    public void deleterIndexOnPage(Page page){
        List<Index> indexes = indexRepository.findByPage(page);
        List<Lemma> lemmas = new ArrayList<>();
        for(Index index : indexes){
            Lemma lemma = index.getLemma();
            lemma.setFrequency(lemma.getFrequency() - 1);
            lemmas.add(lemma);
        }
        indexRepository.deleteAll(indexes);
        lemmaRepository.saveAll(lemmas);
    }
    public void deleterIndexOnSite(Page page){
        List<Index> indexes = indexRepository.findByPage(page);
        indexRepository.deleteAll(indexes);
    }
    public Page save(Page page) {
        save(page.getSite(), Site.Status.INDEXING);
        return pageRepository.save(page);
    }
    public void save(Site site, Site.Status status){
        site.setStatus(status);
        site.setStatusTime(new Date(System.currentTimeMillis()));
        siteRepository.save(site);
    }
    public void save(Site site, String error) {
        site.setLastError(error);
        save(site, Site.Status.FAILED);
    }
    public Lemma save(Lemma lemma){
        return lemmaRepository.save(lemma);
    }
    public void saveAll(ArrayList<Lemma> lemmas){
        lemmaRepository.saveAll(lemmas);
    }
    public Page save(String url, int statusCode, String content, Site site){
        Page page = new Page();
        page.setPath(url);
        page.setCode(statusCode);
        page.setContent(content);
        page.setSite(site);
        return save(page);
    }
    public void writeAll(List<Index> indexes){
        indexRepository.saveAll(indexes);
    }
    public ArrayList<Lemma> searchLemmas(String siteUrl, Set<String> lemmaSet){
        try {
            Site site = siteRepository.findByUrl(siteUrl).get(0);
            return lemmaRepository.findByLemmaInAndSiteOrderByFrequencyAsc(lemmaSet, site);
        } catch (IndexOutOfBoundsException e){
            return lemmaRepository.findByLemmaInOrderByFrequencyAsc(lemmaSet);
        }
    }
    public Site findSiteByUrl(String url){
        return siteRepository.findByUrl(url).get(0);
    }
    public Page findPageByPathAndSite(String siteUrl, String path){
        return pageRepository.findByPathAndSite(path, findSiteByUrl(siteUrl));
    }
    public Page findPageByPathAndSite(Site site, String path){
        return pageRepository.findByPathAndSite(path, site);
    }
    public List<Index> findIndexesByLemma(Lemma lemma){
        return indexRepository.findByLemma(lemma);
    }
    public boolean existsIndexesByLemmaAndPage(Lemma lemma, Page page){
        return indexRepository.existsByLemmaAndPage(lemma, page);
    }
    public ArrayList<Index> findIndexesByLemmasAndPage(ArrayList<Lemma> lemmas, Page page){
        return indexRepository.findByLemmaInAndPage(lemmas, page);
    }
    public Lemma findLemmaByLemmaAndSite(String lemma, Site site) throws Exception{
        return lemmaRepository.findByLemmaAndSite(lemma, site);
    }
}

