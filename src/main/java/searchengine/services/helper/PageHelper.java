package searchengine.services.helper;

import lombok.Getter;
import lombok.Setter;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class PageHelper {
    private Page page;
    private List<Lemma> listLemma = new ArrayList<>();
    private List<Index> listIndex = new ArrayList<>();
    private long relevance = 0;

    public PageHelper(Page page) {
        this.page = page;
    }
}
