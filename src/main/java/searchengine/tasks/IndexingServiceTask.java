package searchengine.tasks;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.CommonConfiguration;
import searchengine.model.Page;
import searchengine.model.SiteModelComparator;
import searchengine.model.SiteModel;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingServiceTask extends Thread {

    private final CommonConfiguration common;
    @Autowired
    private SiteRepository siteRepository;

    @Override
    public void run() {
        try {
            Iterable<SiteModel> siteIterable = siteRepository.findAll();
            List<SiteModel> oldSites = new ArrayList<>();
            siteIterable.forEach(temp -> oldSites.add(temp));
            List<Site> sitesList = common.getSites();
            updateSites(oldSites, sitesList);
        } catch (Exception ex) {
            common.getLogger().log(Level.ERROR, ex.getMessage());
        }
    }

    @Transactional
    public void updateSites(List<SiteModel> oldSites, List<Site> sitesList) {
        SiteModelComparator siteModelComparator = new SiteModelComparator();
        oldSites.sort(siteModelComparator);
        sitesList.forEach(site -> {
            SiteModel siteModel = new SiteModel(site);
            if (oldSites.size() > 0) {
                try {
                    SiteModel oldSite = oldSites.get(Collections.binarySearch(oldSites, siteModel, siteModelComparator));
                    siteRepository.delete(oldSite);
                } catch (Exception ex) {
                    common.getLogger().log(Level.ERROR, ex.getMessage());
                }
            }
            siteRepository.save(siteModel);
            indexing(siteModel);
            siteModel.renew();
        });
    }

    @Transactional
    public void indexing(SiteModel siteModel) {
        try {
            Page page = new Page(siteModel, siteModel.getUrl());
            SiteIndexing siteIndexing = new SiteIndexing(common, page);
            siteIndexing.start();
        } catch (IOException e) {
            common.getLogger().log(Level.ERROR, e.getMessage());
        }
    }
}
