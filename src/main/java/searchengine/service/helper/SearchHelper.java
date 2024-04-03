package searchengine.service.helper;

import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.repository.IndexRepository;
import searchengine.service.impl.SearchingServiceImpl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class SearchHelper {

    private Lemma lemma;
    private Parent parent;
    private IndexRepository indexRepository;
    private final static Logger logger = LogManager.getLogger(SearchingServiceImpl.class);
    private List<PageHelper> pageContainer = new ArrayList<>();
    public SearchHelper(Lemma lemma, IndexRepository indexRepository, Parent parent){
        this.lemma = lemma;
        this.indexRepository = indexRepository;
        this.parent = parent;
    }

    public void addListPage() {
        List<Index> listIndex = indexRepository.findAllByLemma_id(this.getLemma().getId());
        PagesHelperComparator comparator = new PagesHelperComparator();
        listIndex.forEach(index -> {
            PageHelper temp = new PageHelper(index.getPage());
            parent.getPageContainer().sort(comparator);
            int i = Collections.binarySearch(parent.getPageContainer(), temp, comparator);
            if (i < 0) {
                executeNewPage(temp, this.getLemma(), index);
            }
        });
    }

    public void executeNewPage(PageHelper page, Lemma lemma, Index index) {
        page.getListLemma().add(lemma);
        page.getListIndex().add(index);
        page.setRelevance(index.getRank());
        this.pageContainer.add(page);
        parent.getPageContainer().add(page);
    }

}
