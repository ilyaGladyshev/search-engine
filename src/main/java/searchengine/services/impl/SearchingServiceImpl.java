package searchengine.services.impl;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
import searchengine.services.helpers.PageContainer;

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class SearchingServiceImpl implements SearchingService {
    private final static int SNIPPET_WORDS = 10;
    private final static int LEMMA_FREQUENCY = 15;
    private final static String FINAL_TAG_BEGIN = "<title>";
    private final static String FINAL_TAG_END = "</title>";
    private final static int TAG_TITLE_LENGTH = 7;
    private final CommonConfiguration common;
    private final Logger logger = LogManager.getLogger();
    @Autowired
    private final LemmaRepository lemmaRepository;
    @Autowired
    private final SiteRepository siteRepository;
    @Autowired
    private final IndexRepository indexRepository;
    private PageContainer pageContainer;

    @Override
    public SearchingResponse searchingResponse(String query, int offset,
                                               int limit, String site) {
        SearchingResponse searchingResponce = new SearchingResponse();
        logger.log(Level.INFO, "Начат поиск по ключевым словам " + query);
        try {
            CommonLemmatisationHelper commonLemmatisationHelper = new CommonLemmatisationHelper(common.luceneMorphology());
            Map<String, Integer> listLemmas = commonLemmatisationHelper.getLemmasByPageText(query);
            List<Lemma> listLemmaModel = getListLemmaModel(listLemmas, site);
            pageContainer = new PageContainer();
            searchingResponce = getSearchResponse(listLemmaModel);
        } catch (Exception e) {
            searchingResponce.setResult(false);
            searchingResponce.setError("Задан некорректный поисковый запрос");
        }
        return searchingResponce;
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

    public void executeOldPage(PageHelper page, Lemma lemma, Index index) {
        page.getListLemma().add(lemma);
        page.getListIndex().add(index);
        page.setRelevance(page.getRelevance() + index.getRank());
    }

    public void executeNewPage(PageHelper page, Lemma lemma, Index index) {
        page.getListLemma().add(lemma);
        page.getListIndex().add(index);
        page.setRelevance(index.getRank());
        pageContainer.getListPages().add(page);
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
            tempList.stream().filter(t -> (t.getFrequency() < LEMMA_FREQUENCY))
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
        return searchingResponse;
    }

    private void findLemmaInPages(List<Lemma> lemmaList, SearchingResponse searchingResponse) throws IOException {
        int count = 0;
        lemmaList.forEach(this::addListPage);
        CommonLemmatisationHelper commonLemmatisationHelper = new CommonLemmatisationHelper(common.luceneMorphology());
        long maxRelevance = getMaxRelevance();
        for (PageHelper pageHelper : pageContainer.getListPages()) {
            String content = commonLemmatisationHelper.getRussianText(pageHelper.getPage().getContent());
            String[] words = content.split(" ");
            for (Index i : pageHelper.getListIndex()) {
                SnippetClass snippet = getSnippet(words, i.getLemma().getLemma());
                SearchingData data = new SearchingData(i, pageHelper.getPage(),
                        snippet.text(), (double) pageHelper.getRelevance() / maxRelevance);
                data.setTitle(getTitle(i.getPage().getContent()));
                searchingResponse.getData().add(data);
                words = Arrays.copyOfRange(words, snippet.index(), words.length);
                count++;
            }
        }
        searchingResponse.setCount(count);
    }

    private SnippetClass getSnippet(String[] words, String text) throws IOException {
        String str = "";
        int foundedInd = 0;
        CommonLemmatisationHelper commonLemmatisationHelper = new CommonLemmatisationHelper(common.luceneMorphology());
        foundedInd = getFoundedInd(words, text, commonLemmatisationHelper, foundedInd);
        int start = foundedInd - SNIPPET_WORDS > 0 ? foundedInd - TAG_TITLE_LENGTH : 0;
        int fin = foundedInd + SNIPPET_WORDS < words.length ? foundedInd + TAG_TITLE_LENGTH : words.length;
        for (int i = start; i < fin; i++) {
            str += i != foundedInd ? " " + words[i] : " <b>" + words[i] + "</b>";
        }
        return new SnippetClass(str.substring(1), foundedInd);
    }

    private static int getFoundedInd(String[] words, String text,
                                     CommonLemmatisationHelper commonLemmatisationHelper, int foundedInd) {
        int index = 0;
        Map<String, Integer> listLemmasSearch = commonLemmatisationHelper.getLemmasByPageText(text);
        for (String lemma : listLemmasSearch.keySet()) {
            for (String word : words) {
                Map<String, Integer> listLemmasWords = commonLemmatisationHelper.getLemmasByPageText(word);
                for (String lemmaWord : listLemmasWords.keySet()) {
                    if (lemmaWord.toLowerCase().equals(lemma)) {
                        foundedInd = index;
                        break;
                    }
                }
                index++;
            }
        }
        return foundedInd;
    }

    private void addListPage(Lemma lemma) {
        List<Index> listIndex = indexRepository.findAllByLemma_id(lemma.getId());
        PagesHelperComparator comparator = new PagesHelperComparator();
        listIndex.forEach(index -> {
            if (index.getPage().getContent().toLowerCase().contains(lemma.getLemma())) {
                PageHelper temp = new PageHelper(index.getPage());
                pageContainer.getListPages().sort(comparator);
                int i = Collections.binarySearch(pageContainer.getListPages(), temp, comparator);
                if (i >= 0) {
                    executeOldPage(pageContainer.getListPages().get(i), lemma, index);
                } else {
                    executeNewPage(temp, lemma, index);
                }
            }
        });
    }

    private long getMaxRelevance() {
        long result = 0;
        for (PageHelper pageHelper : pageContainer.getListPages()) {
            if (pageHelper.getRelevance() > result) {
                result = pageHelper.getRelevance();
            }
        }
        return result;
    }
}
