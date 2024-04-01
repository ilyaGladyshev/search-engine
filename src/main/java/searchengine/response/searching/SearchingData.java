package searchengine.response.searching;

import lombok.Data;

@Data
public class SearchingData {
    private String site;
    private String siteName;
    private String uri;
    private String snippet;
    private double relevance;
    private String title;
}
