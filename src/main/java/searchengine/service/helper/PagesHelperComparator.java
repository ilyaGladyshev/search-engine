package searchengine.service.helper;

import java.util.Comparator;

public class PagesHelperComparator implements Comparator<PageHelper> {
    @Override
    public int compare(PageHelper o1, PageHelper o2) {
        return o1.getPage().getPath().compareTo(o2.getPage().getPath());
    }
}
