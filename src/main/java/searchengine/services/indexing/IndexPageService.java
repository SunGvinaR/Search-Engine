package searchengine.services.indexing;

import searchengine.dto.indexing.IndexPage;

public interface IndexPageService {
    IndexPage getResult(String url);
}
