package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.StopResult;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class StopIndexingServiceImpl implements StopIndexingService {
    @Override
    public StopResult getResult()
    {
        StopResult stopResult = new StopResult();
            if (Indexer.getStatus() == Indexer.Status.FullIndexing){
                stopResult.setResult(true);
                Indexer.setStatus(Indexer.Status.Stops);
            } else {
                stopResult.setResult(false);
                stopResult.setError("Индексация не запущена");
            }
        return stopResult;
    }
}
