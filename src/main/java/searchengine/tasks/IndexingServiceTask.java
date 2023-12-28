package searchengine.tasks;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.Application;
import searchengine.config.Site;
import searchengine.config.CommonConfiguration;
import searchengine.model.Page;
import searchengine.model.SiteModelComparator;
import searchengine.model.SiteModel;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class IndexingServiceTask extends Thread {

    private final CommonConfiguration common;

    private final Logger logger = LogManager.getLogger(Application.class);

    @Autowired
    private final SiteRepository siteRepository;

    @Autowired
    private final PageRepository pageRepository;

    @Autowired
    private final LemmaRepository lemmaRepository;

    @Autowired
    private final IndexRepository indexRepository;

    @Override
    public void run() {
        try {
            Iterable<SiteModel> siteIterable = siteRepository.findAll();
            List<SiteModel> oldSites = new ArrayList<>();
            siteIterable.forEach(temp -> oldSites.add(temp));
            List<Site> sitesList = common.getSites();
            updateSites(oldSites, sitesList);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage());
        }
    }

    @Transactional
    public void updateSites(List<SiteModel> oldSites, List<Site> sitesList) {
        SiteModelComparator siteModelComparator = new SiteModelComparator();
        oldSites.sort(siteModelComparator);
        sitesList.forEach(site -> {
            SiteModel siteModel = getOldSiteModel(oldSites, siteModelComparator, site);
            siteRepository.save(siteModel);
            indexSiteModel(siteModel);
            siteModel.renew();
        });
    }

    private SiteModel getOldSiteModel(List<SiteModel> oldSites, SiteModelComparator siteModelComparator, Site site) {
        SiteModel siteModel = new SiteModel(site);
        if (oldSites.size() > 0) {
            try {
                SiteModel oldSite = oldSites.get(Collections.binarySearch(oldSites, siteModel, siteModelComparator));
                siteRepository.delete(oldSite);
            } catch (Exception ex) {
                logger.log(Level.ERROR, ex.getMessage());
            }
        }
        return siteModel;
    }

    public void indexSiteModel(SiteModel siteModel) {
        try {
            Page page = new Page(siteModel, siteModel.getUrl());
            SiteIndexing siteIndexing = new SiteIndexing(common, page, siteRepository,
                    pageRepository, lemmaRepository, indexRepository);
            siteIndexing.start();
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage());
        }
    }
}
