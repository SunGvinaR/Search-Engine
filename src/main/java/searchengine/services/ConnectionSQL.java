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
        return pageRepository.findBySiteAndUrl(site, url).isEmpty();
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
    public void deleterPage(List<Page> pages){
        pages.forEach(this::deleterIndexOnPage);
        pageRepository.deleteAll(pages);
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
    public Page write(Page page) {
        write(page.getSite(), Site.Status.INDEXING);
        return pageRepository.save(page);
    }
    public void write(Site site, Site.Status status){
        site.setStatus(status);
        site.setStatusTime(new Date(System.currentTimeMillis()));
        siteRepository.save(site);
    }
    public void write(Site site,  String error) {
        site.setLastError(error);
        write(site, Site.Status.FAILED);
    }
    public Page writePage(String url, int statusCode, String content, Site site){
        Page page = new Page();
        page.setPath(url);
        page.setCode(statusCode);
        page.setContent(content);
        page.setSite(site);
        return write(page);
    }
    public Index write(Index index){
        return indexRepository.save(index);
    }
    public List<Index> writeAll(List<Index> indexes){
        return indexRepository.saveAll(indexes);
    }
}
