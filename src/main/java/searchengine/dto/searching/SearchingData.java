package searchengine.dto.searching;

import lombok.Data;
import searchengine.model.Index;

@Data
public class SearchingData {
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private double relevance;

    public SearchingData(Index index, String snippet, double relevance) {
        this.site = index.getPage().getSite().getUrl();
        this.siteName = index.getPage().getSite().getName();
        this.uri = index.getPage().getPath();
        this.snippet = snippet;
        this.relevance = relevance;
    }
}
