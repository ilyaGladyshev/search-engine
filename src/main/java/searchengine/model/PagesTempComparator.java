package searchengine.model;

import java.util.Comparator;

public class PagesTempComparator implements Comparator<PageTemp> {
    @Override
    public int compare(PageTemp o1, PageTemp o2) {
        return o1.getPage().getPath().compareTo(o2.getPage().getPath());
    }
}
