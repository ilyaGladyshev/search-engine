package searchengine.service.helper;

import lombok.Data;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.repository.IndexRepository;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class SearchHelper {

    private Lemma lemma;
    private IndexRepository indexRepository;
    private List<PageHelper> pageContainer = new ArrayList<>();
    public SearchHelper(Lemma lemma, IndexRepository indexRepository){
        this.lemma = lemma;
        this.indexRepository = indexRepository;
    }

    public void addListPage() {
        List<Index> listIndex = indexRepository.findAllByLemma_id(this.getLemma().getId());
        PagesHelperComparator comparator = new PagesHelperComparator();
        listIndex.forEach(index -> {
            PageHelper temp = new PageHelper(index.getPage());
            this.getPageContainer().sort(comparator);
            int i = Collections.binarySearch(this.getPageContainer(), temp, comparator);
            if (i >= 0) {
                executeOldPage(this.getPageContainer().get(i), this.getLemma(), index);
            } else {
                executeNewPage(temp, this.getLemma(), index);
            }
        });
    }

    public void executeOldPage(PageHelper page, Lemma lemma, Index index) {
        page.getListLemma().add(lemma);
        page.getListIndex().add(index);
        page.setRelevance(page.getRelevance() + index.getRank());
    }

    public void executeNewPage(PageHelper page, Lemma lemma, Index index) {
        page.getListLemma().add(lemma);
        page.getListIndex().add(index);
        page.setRelevance(index.getRank());
        pageContainer.add(page);
    }

}
