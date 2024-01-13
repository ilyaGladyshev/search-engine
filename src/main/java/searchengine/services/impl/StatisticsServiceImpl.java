package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.CommonConfiguration;
import searchengine.responses.statistics.DetailedStatisticsItem;
import searchengine.responses.statistics.StatisticsData;
import searchengine.responses.statistics.StatisticsResponse;
import searchengine.responses.statistics.TotalStatistics;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.StatisticsService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final CommonConfiguration common;

    private String formatErrorData(String errorData) {
        return (errorData == null) ? "" : errorData;
    }

    @Autowired
    private final LemmaRepository lemmaRepository;

    @Autowired
    private final SiteRepository siteRepository;

    @Autowired
    private final PageRepository pageRepository;

    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = new TotalStatistics();
        total.setSites(common.getSites().size());
        total.setIndexing(true);
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = common.getSites();
        executeSitesList(sitesList, total, detailed);
        StatisticsResponse response = getStatisticsResponse(total, detailed);
        return response;
    }

    private static StatisticsResponse getStatisticsResponse(TotalStatistics total, List<DetailedStatisticsItem> detailed) {
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }

    private void executeSitesList(List<Site> sitesList, TotalStatistics total, List<DetailedStatisticsItem> detailed) {
        for (Site site : sitesList) {
            int pages = pageRepository.getPagesCount(site.getUrl());
            int lemmas = lemmaRepository.getLemmaCount(site.getUrl());
            DetailedStatisticsItem item = getStatisticsItem(site, pages, lemmas);
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }
    }

    private DetailedStatisticsItem getStatisticsItem(Site site, int pages, int lemmas) {
        DetailedStatisticsItem item = new DetailedStatisticsItem();
        item.setName(site.getName());
        item.setUrl(site.getUrl());
        item.setPages(pages);
        item.setLemmas(lemmas);
        item.setError(formatErrorData(siteRepository.getLastErrorByUrl(site.getUrl())));
        item.setStatus(siteRepository.getStatusByUrl(site.getUrl()));
        if (siteRepository.getStatusTimeByUrl(site.getUrl()) != null) {
            ZonedDateTime zdt = ZonedDateTime.of(siteRepository.getStatusTimeByUrl(site.getUrl()), ZoneId.systemDefault());
            item.setStatusTime(zdt.toInstant().toEpochMilli());
        } else {
            item.setStatusTime(0);
        }
        return item;
    }
}
