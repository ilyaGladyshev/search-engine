package searchengine.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.CommonConfiguration;
import searchengine.model.SiteModel;
import searchengine.response.statistic.DetailedStatisticsItem;
import searchengine.response.statistic.StatisticsData;
import searchengine.response.statistic.StatisticsResponse;
import searchengine.response.statistic.TotalStatistics;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.service.StatisticsService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final CommonConfiguration common;
    @Autowired
    private final LemmaRepository lemmaRepository;
    @Autowired
    private final SiteRepository siteRepository;
    @Autowired
    private final PageRepository pageRepository;

    private String formatErrorData(String errorData) {
        return (errorData == null) ? "" : errorData;
    }

    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = new TotalStatistics();
        total.setSites(common.getSites().size());
        total.setIndexing(true);
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = common.getSites();
        executeSitesList(sitesList, total, detailed);
        return getStatisticsResponse(total, detailed);
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
            int pages = pageRepository.countAllBySite_Url(site.getUrl());
            int lemmas = lemmaRepository.countAllBySite_Url(site.getUrl());
            DetailedStatisticsItem item = getStatisticsItem(site, pages, lemmas);
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }
    }

    private DetailedStatisticsItem getStatisticsItem(Site site, int pages, int lemmas) {
        List<SiteModel> listSiteModel = siteRepository.findAllByUrl(site.getUrl());
        DetailedStatisticsItem item = new DetailedStatisticsItem();
        item.setName(site.getName());
        item.setUrl(site.getUrl());
        item.setPages(pages);
        item.setLemmas(lemmas);
        item.setError(formatErrorData(listSiteModel.get(0).getLastError()));
        item.setStatus(listSiteModel.get(0).getStatus().name());
        if (listSiteModel.get(0).getStatusTime() != null) {
            ZonedDateTime zdt = ZonedDateTime.of(listSiteModel.get(0).getStatusTime(), ZoneId.systemDefault());
            item.setStatusTime(zdt.toInstant().toEpochMilli());
        } else {
            item.setStatusTime(0);
        }
        return item;
    }
}
