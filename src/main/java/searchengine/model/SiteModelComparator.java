package searchengine.model;

import java.util.Comparator;

public class SiteModelComparator implements Comparator<SiteModel> {
    @Override
    public int compare(SiteModel o1, SiteModel o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
