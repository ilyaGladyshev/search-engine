package searchengine.service.helper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@RequiredArgsConstructor
public class PageHelper {
    private final Page page;
    private List<Lemma> listLemma = new ArrayList<>();
    private List<Index> listIndex = new ArrayList<>();
    private long relevance;
}
