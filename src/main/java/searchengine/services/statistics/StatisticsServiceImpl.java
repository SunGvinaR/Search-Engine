package searchengine.services.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Site;
import searchengine.services.ConnectionSQL;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final ConnectionSQL connectionSQL;

    @Override
    public StatisticsResponse getStatistics() {

        TotalStatistics total = new TotalStatistics();
        total.setSites((int) connectionSQL.getSiteRepository().count());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = connectionSQL.getSiteRepository().findAll();
        for (Site site : sitesList) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            int pages = connectionSQL.getPageRepository().findBySite(site).size();
            int lemmas = connectionSQL.getLemmaRepository().findBySite(site).size();
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(site.getStatus().toString());
            item.setError(site.getLastError());
            item.setStatusTime(site.getStatusTime().getTime());
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
