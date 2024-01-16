package searchengine.services.impl;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.Application;
import searchengine.config.CommonConfiguration;
import searchengine.responses.searching.SearchingData;
import searchengine.responses.searching.SearchingDataComparator;
import searchengine.responses.searching.SearchingResponse;
import searchengine.model.SiteModel;
import searchengine.model.Lemma;
import searchengine.model.Index;
import searchengine.model.LemmaComparator;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.SearchingService;
import searchengine.services.helpers.CommonLemmatisationHelper;
import searchengine.services.helpers.PageHelper;
import searchengine.services.helpers.PagesHelperComparator;
import searchengine.services.helpers.SnippetClass;

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class SearchingServiceImpl implements SearchingService {

    private final CommonConfiguration common;
    private final Logger logger = LogManager.getLogger(Application.class);
    private final String FINAL_TAG_BEGIN = "<title>";
    private final String FINAL_TAG_END = "</title>";

    private final int TAG_TITLE_LENGTH = 7;
    private final List<PageHelper> pages = new ArrayList<>();

    @Autowired
    private final LemmaRepository lemmaRepository;

    @Autowired
    private final SiteRepository siteRepository;

    @Autowired
    private final IndexRepository indexRepository;

    @Override
    public SearchingResponse searchingResponse(String query, int offset,
                                               int limit, String site) {
        SearchingResponse searchingResponce = new SearchingResponse();
        logger.log(Level.INFO, "Начат поиск по ключевым словам " + query);
        try {
            CommonLemmatisationHelper commonLemmatisationHelper = new CommonLemmatisationHelper(common.luceneMorphology());
            Map<String, Integer> listLemmas = commonLemmatisationHelper.getLemmasByPageText(query);
            List<Lemma> listLemmaModel = getListLemmaModel(listLemmas, site);
            searchingResponce = getSearchResponse(listLemmaModel);
        } catch (Exception e) {
            searchingResponce.setResult(false);
            searchingResponce.setError("Задан некорректный поисковый запрос");
        }
        return searchingResponce;
    }

    private List<Lemma> getListLemmaModel(Map<String, Integer> listLemmas, String site) {
        List<Lemma> resultList = new ArrayList<>();
        listLemmas.keySet().forEach(l -> {
            List<Lemma> tempList;
            if (site == null) {
                tempList = lemmaRepository.findAllByLemma(l);
            } else {
                SiteModel s = siteRepository.findAllByUrl(site).get(0);
                tempList = lemmaRepository.findLemmaBySite(l, Integer.toString(s.getId()));
            }
            tempList.stream().filter(t -> (t.getFrequency() < common.getLEMMA_FREQUENCY()))
                    .forEach(resultList::add);
        });
        return resultList;
    }

    private SearchingResponse getSearchResponse(List<Lemma> lemmaList) throws IOException {
        SearchingResponse searchingResponse = new SearchingResponse();
        searchingResponse.setResult(true);
        searchingResponse.setData(new ArrayList<>());
        LemmaComparator comparator = new LemmaComparator();
        lemmaList.sort(comparator);
        findLemmaInPages(lemmaList, searchingResponse);
        SearchingDataComparator dataComparator = new SearchingDataComparator();
        searchingResponse.getData().sort(dataComparator);
        pages.clear();
        return searchingResponse;
    }

    private void findLemmaInPages(List<Lemma> lemmaList, SearchingResponse searchingResponse) throws IOException {
        int count = 0;
        lemmaList.forEach(this::addListPage);
        CommonLemmatisationHelper commonLemmatisationHelper = new CommonLemmatisationHelper(common.luceneMorphology());
        long maxRelevance = getMaxRelevance();
        for (PageHelper pageHelper : pages) {
            String content = commonLemmatisationHelper.getRussianText(pageHelper.getPage().getContent());
            String[] words = content.split(" ");
            for (Index i : pageHelper.getListIndex()) {
                SnippetClass snippet = getSnippet(words, i.getLemma().getLemma());
                SearchingData data = new SearchingData(i,
                        snippet.text(), (double) pageHelper.getRelevance() / maxRelevance);
                data.setTitle(getTitle(i.getPage().getContent()));
                searchingResponse.getData().add(data);
                words = Arrays.copyOfRange(words, snippet.index(), words.length);
                count++;
            }
        }
        searchingResponse.setCount(count);
    }

    public String getTitle(String content) {
        String result = "";
        int start = content.indexOf(FINAL_TAG_BEGIN) + TAG_TITLE_LENGTH;
        int fin = content.indexOf(FINAL_TAG_END);
        try {
            result = content.substring(start, fin);
        } catch (Exception ignored) {
            logger.log(Level.ERROR, "Ошибка при получении заголовка");
        }
        return result;
    }

    private SnippetClass getSnippet(String[] words, String text) {
        String str = "";
        int index = 0;
        int foundedInd = 0;
        for (String word : words) {
            if (word.toLowerCase().contains(text)) {
                foundedInd = index;
            }
            index++;
        }
        int start = foundedInd - common.getSNIPPET_WORDS() > 0 ? foundedInd - TAG_TITLE_LENGTH : 0;
        int fin = foundedInd + common.getSNIPPET_WORDS() < words.length ? foundedInd + TAG_TITLE_LENGTH : words.length;
        for (int i = start; i < fin; i++) {
            str += i != foundedInd ? " " + words[i] : " <b>" + words[i] + "</b>";
        }
        return new SnippetClass(str.substring(1), foundedInd);
    }

    private void addListPage(Lemma lemma) {
        List<Index> listIndex = indexRepository.findAllByLemma_id(lemma.getId());
        PagesHelperComparator comparator = new PagesHelperComparator();
        listIndex.forEach(index -> {
            if (index.getPage().getContent().toLowerCase().contains(lemma.getLemma())) {
                PageHelper temp = new PageHelper(index.getPage());
                pages.sort(comparator);
                int i = Collections.binarySearch(pages, temp, comparator);
                if (i >= 0) {
                    executeOldPage(pages.get(i), lemma, index);
                } else {
                    executeNewPage(temp, lemma, index);
                }
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
        pages.add(page);
    }

    private long getMaxRelevance() {
        long result = 0;
        for (PageHelper pageHelper : pages) {
            if (pageHelper.getRelevance() > result) {
                result = pageHelper.getRelevance();
            }
        }
        return result;
    }
}
