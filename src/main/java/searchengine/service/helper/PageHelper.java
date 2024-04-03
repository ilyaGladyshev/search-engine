package searchengine.service.helper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.service.impl.SearchingServiceImpl;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@RequiredArgsConstructor
public class PageHelper {
    private final static Logger logger = LogManager.getLogger(SearchingServiceImpl.class);
    private final Page page;
    private List<Lemma> listLemma = new ArrayList<>();
    private List<Index> listIndex = new ArrayList<>();
    private long relevance;
}
