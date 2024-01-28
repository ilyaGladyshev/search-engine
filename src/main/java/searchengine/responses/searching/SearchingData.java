package searchengine.responses.searching;

import lombok.Data;
import searchengine.model.Index;
import searchengine.model.Page;
import searchengine.model.SiteModel;

@Data
public class SearchingData {
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private double relevance;

    public SearchingData(Index index, Page page,
                         String snippet, double relevance) {
        this.site = index.getPage().getSite().getUrl();
        this.siteName = index.getPage().getSite().getName();
        this.uri = page.getPath();
        this.snippet = snippet;
        this.relevance = relevance;
    }
}
