package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.CommonConfiguration;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.services.StatisticsService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final Random random = new Random();
    private final CommonConfiguration common;

    @Override
    public StatisticsResponse getStatistics() {
        String[] statuses = { "INDEXED", "FAILED", "INDEXING" };
        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""
        };

        TotalStatistics total = new TotalStatistics();
        total.setSites(common.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = common.getSites();
        for(int i = 0; i < sitesList.size(); i++) {
            Site site = sitesList.get(i);
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            int pages = common.getPageRepository().getPagesCount(site.getUrl());
            int lemmas = common.getLemmaRepository().getLemmaCount(site.getUrl());
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setError(common.getSiteRepository().getLastErrorByUrl(site.getUrl()));
            item.setStatus(common.getSiteRepository().getStatusByUrl(site.getUrl()));
            if (common.getSiteRepository().getStatusTimeByUrl(site.getUrl())!=null) {
                ZonedDateTime zdt = ZonedDateTime.of(common.getSiteRepository().getStatusTimeByUrl(site.getUrl()), ZoneId.systemDefault());
                item.setStatusTime(zdt.toInstant().toEpochMilli());
            } else {
                item.setStatusTime(0);
            }
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
