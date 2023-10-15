package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.indexing.IndexPage;
import searchengine.dto.indexing.StartResult;
import searchengine.dto.indexing.StopResult;
import searchengine.dto.search.SearchResult;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexing.IndexPageService;
import searchengine.services.indexing.StopIndexingService;
import searchengine.services.indexing.StartIndexingService;
import searchengine.services.search.SearchService;
import searchengine.services.statistics.StatisticsService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;
    private final StartIndexingService startIndexingService;
    private final StopIndexingService stopIndexingService;
    private final IndexPageService indexPageService;
    private final SearchService searchService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
    @GetMapping("/startIndexing")
    public ResponseEntity<StartResult> startIndexing() {
        return ResponseEntity.ok(startIndexingService.getResult());
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity<StopResult> stopIndexing() {
        return ResponseEntity.ok(stopIndexingService.getResult());
    }
    @PostMapping("/indexPage")
    public ResponseEntity<IndexPage> indexPage(String url) {
        return ResponseEntity.ok(indexPageService.getResult(url));
    }
    @GetMapping("/search")
    public ResponseEntity<SearchResult> search(String query, String site, int offset, int limit) {
        return ResponseEntity.ok(searchService.getResult(query, site, offset, limit));
    }

}

