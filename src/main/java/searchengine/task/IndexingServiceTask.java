package searchengine.task;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.config.CommonConfiguration;
import searchengine.model.Page;
import searchengine.model.SiteModelComparator;
import searchengine.model.SiteModel;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Setter
@Scope("prototype")
@RequiredArgsConstructor
public class IndexingServiceTask extends Thread {
    private final static Logger logger = LogManager.getLogger();
    @Autowired
    private final SiteRepository siteRepository;
    @Autowired
    private final CommonConfiguration common;

    @Lookup
    public SiteIndexing createSiteIndexing() {
        return null;
    }

    @Override
    public void run() {
        try {
            Iterable<SiteModel> siteIterable = siteRepository.findAll();
            List<SiteModel> oldSites = new ArrayList<>();
            siteIterable.forEach(oldSites::add);
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

    public void indexSiteModel(SiteModel siteModel) {
        try {
            Page page = new Page(siteModel, siteModel.getUrl());
            SiteIndexing siteIndexing = createSiteIndexing();
            siteIndexing.setPage(page);
            siteIndexing.start();
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage());
        }
    }

    private SiteModel getOldSiteModel(List<SiteModel> oldSites, SiteModelComparator siteModelComparator, Site site) {
        SiteModel siteModel = new SiteModel(site);
        if (!oldSites.isEmpty()) {
            try {
                SiteModel oldSite = oldSites.get(Collections.binarySearch(oldSites, siteModel, siteModelComparator));
                siteRepository.delete(oldSite);
            } catch (Exception ex) {
                logger.log(Level.ERROR, ex.getMessage());
            }
        }
        return siteModel;
    }

}
