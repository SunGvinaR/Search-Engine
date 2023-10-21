package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.StartResult;
import searchengine.model.Site;
import searchengine.model.SitesList1;
import searchengine.services.ConnectionSQL;

import java.util.Vector;

@Service
@RequiredArgsConstructor
public class StartIndexingServiceImpl implements StartIndexingService, Runnable {
    private final ConnectionSQL connectionSQL;
    private final SitesList1 sitesList;
    public static Thread indexing;
    @Override
    public StartResult getResult() {

        StartResult startResult = new StartResult();

        if(Indexer.getStatus() != Indexer.Status.Ready){
            startResult.setResult(false);
            startResult.setError("Индексация уже запущена");
        } else {
            new Thread(this).start();
            startResult.setResult(true);
        }
        return startResult;
    }

    @Override
    public void run() {
        Indexer.setStatus(Indexer.Status.FullIndexing);

        for(Site site : sitesList.getSites()){
            Site siteClone = site.clone();
            connectionSQL.deleterSites(site);

            Indexer indexer = new Indexer("/", siteClone, connectionSQL, new Vector<>());
            connectionSQL.save(siteClone, Site.Status.INDEXING);
            new Thread(indexer).start();
        }
    }
}
