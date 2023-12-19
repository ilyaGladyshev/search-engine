package searchengine.tasks;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.CommonConfiguration;
import searchengine.model.Page;
import searchengine.model.SiteModelComparator;
import searchengine.model.SiteModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingServiceTask extends Thread {
    @Override
    public void run() {
        super.run();
        try {
            Iterable<SiteModel> siteIterable = common.getSiteRepository().findAll();
            ArrayList<SiteModel> oldSites = new ArrayList<>();
            siteIterable.forEach(temp -> oldSites.add(temp));
            SiteModelComparator siteModelComparator = new SiteModelComparator();
            Collections.sort(oldSites, siteModelComparator);
            List<Site> sitesList = common.getSites();
            updateSites(oldSites, sitesList, siteModelComparator);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private final CommonConfiguration common;

    @Transactional
    public void updateSites(ArrayList<SiteModel> oldSites, List<Site> sitesList,
                            SiteModelComparator siteModelComparator){
        sitesList.forEach(site -> {
            SiteModel siteModel = new SiteModel(site);
            if (oldSites.size()>0){
                try {
                    SiteModel oldSite = oldSites.get(Collections.binarySearch(oldSites, siteModel, siteModelComparator));
                    common.getSiteRepository().delete(oldSite);
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
            common.getSiteRepository().save(siteModel);
            Page page = null;
            try {
                page = new Page(siteModel, siteModel.getUrl(), common);
                SiteIndexing siteIndexing = new SiteIndexing(common, page);
                siteIndexing.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
