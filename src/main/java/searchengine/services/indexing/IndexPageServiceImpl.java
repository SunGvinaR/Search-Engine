package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.IndexPage;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.SitesList1;
import searchengine.services.ConnectionSQL;

import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class IndexPageServiceImpl implements IndexPageService {

    private final ConnectionSQL connectionSQL;
    private final SitesList1 sites;

    public IndexPage getResult(String url){
        IndexPage indexPage = new IndexPage();
        if (!Indexer.getStatus().equals(Indexer.Status.Ready)){
            indexPage.setResult(false);
            indexPage.setError("Индексация уже запущена");
            return indexPage;
        }

        Site site = null;
        String siteUrl = "";

        Pattern pattern = Pattern.compile("https?://[^/]+");
        Matcher matcher = pattern.matcher(url);

        while (matcher.find()){
            siteUrl = matcher.group();
        }

        String page = url.replace(siteUrl, "");

        for (Site s : sites.getSites()) {
            if (s.getUrl().equals(siteUrl)) {
                List<Site> sites = connectionSQL.getSiteRepository().findByUrl(siteUrl);
                if (!sites.isEmpty()) {
                    site = sites.get(0);
                    Page pages = connectionSQL.getPageRepository().findByPathAndSite(page, site);
                    connectionSQL.getPageRepository().delete(pages);
                } else {
                    site = s.clone();
                    connectionSQL.write(site, Site.Status.INDEXING);
                }
            }
        }
        if(site != null){
            Indexer indexer = new Indexer(page, site, connectionSQL, new Vector<>());
            Indexer.setStatus(Indexer.Status.OnePageIndexing);

            indexer.run();

            indexPage.setResult(true);
        } else {
            indexPage.setResult(false);
            indexPage.setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
        return indexPage;
    }
}
