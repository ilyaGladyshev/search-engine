package searchengine.service.impl;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.CommonConfiguration;
import searchengine.model.*;
import searchengine.response.searching.SearchingData;
import searchengine.response.searching.SearchingDataComparator;
import searchengine.response.searching.SearchingResponse;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.SiteRepository;
import searchengine.service.SearchingService;
import searchengine.service.helper.*;

import java.io.IOException;
import java.util.*;


@Service
@RequiredArgsConstructor
public class SearchingServiceImpl implements SearchingService {
    private final static int SNIPPET_WORDS = 10;
    private final static int LEMMA_FREQUENCY = 500;
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
    private List<Parent> parentList = new ArrayList<>();

    @Override
    public SearchingResponse searchingResponse(String query, int limit,
                                               int offset, String site) {
        parentList = new ArrayList<>();
        SearchingResponse searchingResponse = new SearchingResponse();
        logger.log(Level.INFO, "Начат поиск по ключевым словам");
        try {
            LuceneMorphology luceneMorphology = common.luceneMorphology();
            CommonLemmaHelper commonLemmaHelper = new CommonLemmaHelper(luceneMorphology);
            Map<String, Integer> listLemmas = commonLemmaHelper.getLemmasByPageText(query);
            getListLemmaModel(listLemmas, site);
            pageContainer = new PageContainer();
            sortListLemmaModel();
            searchingResponse = getSearchResponse();
        } catch (Exception e) {
            searchingResponse.setResult(false);
            searchingResponse.setError("Задан некорректный поисковый запрос");
        }
        return searchingResponse;
    }

    public void sortListLemmaModel(){
        parentList.forEach(parent -> parent.getSearchHelpers().
                forEach(SearchHelper::addListPage));
        ParentComparator parentComparator = new ParentComparator();
        this.parentList.sort(parentComparator);
        if (!parentList.isEmpty()) {
            List<PageHelper> sortResult = getUnitedResult();
            sortResult.forEach(r -> pageContainer.getListPages().add(r));
        }
    }

    private List<PageHelper> getUnitedResult() {
        PagesHelperComparator pagesHelperComparator = new PagesHelperComparator();
        List<PageHelper> tempResult = parentList.get(0).getPageContainer();
        parentList.forEach(parent -> parent.getPageContainer().sort(pagesHelperComparator));
        return compareLemma(tempResult, pagesHelperComparator);
    }

    private List<PageHelper> compareLemma(List<PageHelper> tempResult, PagesHelperComparator pagesHelperComparator) {
        List<PageHelper> result = new ArrayList<>();
        for (PageHelper ph : tempResult) {
            boolean isFounded = true;
            for (Parent parent : this.parentList) {
                int search = Collections.binarySearch(parent.getPageContainer(), ph, pagesHelperComparator);
                if (search < 0) {
                    isFounded = false;
                    break;
                }
            }
            if (isFounded) {
                result.add(ph);
            }
        }
        return result;
    }

    public String getTitle(String content) {
        String result = "";
        if (content.indexOf(FINAL_TAG_BEGIN)>0) {
            int start = content.indexOf(FINAL_TAG_BEGIN) + TAG_TITLE_LENGTH;
            int fin = content.indexOf(FINAL_TAG_END);
            result = content.substring(start, fin);
        }
        return result;
    }

    private void getListLemmaModel(Map<String, Integer> listLemmas, String site) {
        listLemmas.keySet().forEach(l -> {
            Parent parent = new Parent(l);
            List<Lemma> tempList;
            if (site == null) {
                tempList = lemmaRepository.findAllByLemma(l);
            } else {
                SiteModel s = siteRepository.findAllByUrl(site).get(0);
                tempList = lemmaRepository.findAllByLemmaAndSite_id(l, s.getId());
            }
            for(Lemma lemma:tempList){
                if (lemma.getFrequency()<LEMMA_FREQUENCY){
                    parent.getSearchHelpers().add(new SearchHelper(lemma, indexRepository, parent));
                }
            }
            parentList.add(parent);
        });
    }

    private SearchingResponse getSearchResponse() throws IOException {
        SearchingResponse searchingResponse = new SearchingResponse();
        searchingResponse.setResult(true);
        searchingResponse.setData(new ArrayList<>());
        findLemmaInPages(searchingResponse);
        SearchingDataComparator dataComparator = new SearchingDataComparator();
        searchingResponse.getData().sort(dataComparator);
        return searchingResponse;
    }

    private void findLemmaInPages(SearchingResponse searchingResponse) throws IOException {
        int count = 0;
        CommonLemmaHelper commonLemmaHelper = new CommonLemmaHelper(common.luceneMorphology());
        long maxRelevance = getMaxRelevance();
        for (PageHelper pageHelper : pageContainer.getListPages()) {
            String content = commonLemmaHelper.getRussianText(pageHelper.getPage().getContent());
            String[] words = content.split(" ");
            for (Index i : pageHelper.getListIndex()) {
                SnippetClass snippet = getSnippet(words, i.getLemma().getLemma());
                double relevance = (double) pageHelper.getRelevance() / maxRelevance;
                SearchingData data = createSearchingData(i, snippet.text(), relevance);
                searchingResponse.getData().add(data);
                words = Arrays.copyOfRange(words, snippet.index(), words.length);
                count++;
            }
        }
        searchingResponse.setCount(count);
    }

    private SearchingData createSearchingData(Index index, String text, double relevance){
        SearchingData data =  new SearchingData();
        data.setSite(index.getPage().getSite().getUrl());
        data.setSiteName(index.getPage().getSite().getName());
        data.setUri(index.getPage().getPath());
        data.setSnippet(text);
        data.setRelevance(relevance);
        data.setTitle(getTitle(index.getPage().getContent()));
        return data;
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
        Map<String, Integer> listLemmasSearch = commonLemmaHelper.getLemmasByPageText(text);
        for (String lemma : listLemmasSearch.keySet()) {
            int index = 0;
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
