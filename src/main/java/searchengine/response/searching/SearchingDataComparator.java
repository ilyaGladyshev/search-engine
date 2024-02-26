package searchengine.response.searching;

import java.util.Comparator;

public class SearchingDataComparator implements Comparator<SearchingData> {
    @Override
    public int compare(SearchingData o1, SearchingData o2) {
        return Double.compare(o2.getRelevance(), o1.getRelevance());
    }
}
