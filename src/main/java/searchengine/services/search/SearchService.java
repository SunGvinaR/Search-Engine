package searchengine.services.search;

import searchengine.dto.search.SearchResult;

public interface SearchService {
    SearchResult getResult(String query, String site, Integer offset, Integer limit);
}
