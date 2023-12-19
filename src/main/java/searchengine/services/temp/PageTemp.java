package searchengine.services.temp;

import lombok.Getter;
import lombok.Setter;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class PageTemp {
    private Page page;
    private List<Lemma> listLemma = new ArrayList<>();
    private List<Index> listIndex = new ArrayList<>();
    private long relevance = 0;

    public PageTemp(Page page) {
        this.page = page;
    }
}
