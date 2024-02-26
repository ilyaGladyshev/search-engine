package searchengine.service.impl;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.CommonConfiguration;
import searchengine.response.searching.SearchingData;
import searchengine.response.searching.SearchingDataComparator;
import searchengine.response.searching.SearchingResponse;
import searchengine.model.SiteModel;
import searchengine.model.Lemma;
import searchengine.model.Index;
import searchengine.model.LemmaComparator;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.SiteRepository;
import searchengine.service.SearchingService;
import searchengine.service.helper.CommonLemmaHelper;
import searchengine.service.helper.PageHelper;
import searchengine.service.helper.PagesHelperComparator;
import searchengine.service.helper.SnippetClass;
import searchengine.service.helper.PageContainer;

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
    private final static Logger logger = LogManager.getLogger(SearchingServiceImpl.class);
    private final CommonConfiguration common;
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
        SearchingResponse searchingResponse = new SearchingResponse();
        logger.log(Level.INFO, "Начат поиск по ключевым словам " + query);
        try {
            LuceneMorphology luceneMorphology = common.luceneMorphology();
            CommonLemmaHelper commonLemmaHelper = new CommonLemmaHelper(luceneMorphology);
            Map<String, Integer> listLemmas = commonLemmaHelper.getLemmasByPageText(query);
            List<Lemma> listLemmaModel = getListLemmaModel(listLemmas, site);
            pageContainer = new PageContainer();
            searchingResponse = getSearchResponse(listLemmaModel);
        } catch (Exception e) {
            searchingResponse.setResult(false);
            searchingResponse.setError("Задан некорректный поисковый запрос");
        }
        return searchingResponse;
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
                tempList = lemmaRepository.findAllByLemmaAndSite_id(l, s.getId());
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
        CommonLemmaHelper commonLemmaHelper = new CommonLemmaHelper(common.luceneMorphology());
        long maxRelevance = getMaxRelevance();
        for (PageHelper pageHelper : pageContainer.getListPages()) {
            String content = commonLemmaHelper.getRussianText(pageHelper.getPage().getContent());
            String[] words = content.split(" ");
            for (Index i : pageHelper.getListIndex()) {
                SnippetClass snippet = getSnippet(words, i.getLemma().getLemma());
                double relevance = (double) pageHelper.getRelevance() / maxRelevance;
                SearchingData data = new SearchingData(i.getPage().getSite().getUrl(),
                        i.getPage().getSite().getName(), i.getPage().getPath(),
                        snippet.text(), relevance, getTitle(i.getPage().getContent()));
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
        CommonLemmaHelper commonLemmaHelper = new CommonLemmaHelper(common.luceneMorphology());
        foundedInd = getFoundedInd(words, text, commonLemmaHelper, foundedInd);
        int start = foundedInd - SNIPPET_WORDS > 0 ? foundedInd - TAG_TITLE_LENGTH : 0;
        int fin = foundedInd + SNIPPET_WORDS < words.length ? foundedInd + TAG_TITLE_LENGTH : words.length;
        for (int i = start; i < fin; i++) {
            str += i != foundedInd ? " " + words[i] : " <b>" + words[i] + "</b>";
        }
        return new SnippetClass(str.substring(1), foundedInd);
    }

    private static int getFoundedInd(String[] words, String text,
                                     CommonLemmaHelper commonLemmaHelper, int foundedInd) {
        int index = 0;
        Map<String, Integer> listLemmasSearch = commonLemmaHelper.getLemmasByPageText(text);
        for (String lemma : listLemmasSearch.keySet()) {
            for (String word : words) {
                Map<String, Integer> listLemmasWords = commonLemmaHelper.getLemmasByPageText(word);
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
