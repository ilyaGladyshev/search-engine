package searchengine.response.searching;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SearchingData {
    private final String site;
    private final String siteName;
    private final String uri;
    private final String snippet;
    private final double relevance;
    private final String title;

}
