package searchengine.service.helper;

import java.util.Comparator;

public class ParentComparator implements Comparator<Parent> {
    @Override
    public int compare(Parent o1, Parent o2) {
        return o1.getPageContainer().size() - o2.getPageContainer().size();
    }
}
