package searchengine.service.helper;

import java.util.Comparator;

public class SearchHelperComparator implements Comparator<SearchHelper> {
    @Override
    public int compare(SearchHelper o1, SearchHelper o2) {
        return o1.getLemma().getFrequency() - o2.getLemma().getFrequency();
    }
}
