package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.springframework.stereotype.Service;
import searchengine.config.CommonConfiguration;
import searchengine.dto.searching.SearchingData;
import searchengine.dto.searching.SearchingDataComparator;
import searchengine.dto.searching.SearchingResponse;
import searchengine.model.*;
import searchengine.services.SearchingService;
import searchengine.services.temp.CommonLemmatization;
import searchengine.services.temp.PageTemp;
import searchengine.services.temp.PagesTempComparator;
import searchengine.services.temp.SnippetClass;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchingServiceImpl implements SearchingService {

    private final CommonConfiguration common;

    private final String finalTagBegin = "<title>";

    private final String finalTagEnd = "</title>";
    private List<PageTemp> pages = new ArrayList<>();

    @Override
    public SearchingResponse searching(String query, int offset,
                                       int limit, String site) {
        SearchingResponse searchingResponce = new SearchingResponse();
        common.getLogger().log(Level.INFO, "Начат поиск по ключевым словам " + query);
        try {
            CommonLemmatization commonLemmatization = new CommonLemmatization(common.luceneMorphology());
            HashMap<String, Integer> listLemmas = commonLemmatization.executePage(query);
            List<Lemma> listLemmaModel = getListLemmaModel(listLemmas, site);
            searchingResponce = getSearchResponse(listLemmaModel);
        } catch (Exception e) {
            searchingResponce.setResult(false);
            searchingResponce.setError("Задан пустой поисковый запрос");
        }
        return searchingResponce;
    }

    private List<Lemma> getListLemmaModel(HashMap<String, Integer> listLemmas, String site) {
        List<Lemma> resultList = new ArrayList<>();
        listLemmas.keySet().forEach(l -> {
            List<Lemma> tempList;
            if (site == null) {
                tempList = common.getLemmaRepository().findAllLemmas(l);
            } else {
                SiteModel s = common.getSiteRepository().findSiteByUrl(site).get(0);
                tempList = common.getLemmaRepository().findLemmaBySite(l, Integer.toString(s.getId()));
            }
            tempList.forEach(t -> {
                if (t.getFrequency() < common.getLemmaFrequency()) {
                    resultList.add(t);
                }
            });
        });
        return resultList;
    }

    private SearchingResponse getSearchResponse(List<Lemma> lemmaList) throws IOException {
        SearchingResponse searchingResponse = new SearchingResponse();
        searchingResponse.setResult(true);
        searchingResponse.setData(new ArrayList<>());
        LemmaComparator comparator = new LemmaComparator();
        lemmaList.sort(comparator);
        parsePage(lemmaList, searchingResponse);
        SearchingDataComparator dataComparator = new SearchingDataComparator();
        searchingResponse.getData().sort(dataComparator);
        pages.clear();
        return searchingResponse;
    }

    private void parsePage(List<Lemma> lemmaList, SearchingResponse searchingResponse) throws IOException {
        int count = 0;
        lemmaList.forEach(l -> addListPage(l));
        CommonLemmatization commonLemmatization = new CommonLemmatization(common.luceneMorphology());
        long maxRelevance = getMaxRelevance();
        for (PageTemp pageTemp : pages) {
            String content = commonLemmatization.getRussianText(pageTemp.getPage().getContent());
            String[] words = content.split(" ");
            for (Index i : pageTemp.getListIndex()) {
                SnippetClass snippet = getSnippet(words, i.getLemma().getLemma());
                SearchingData data = new SearchingData(i,
                        snippet.text(), (double) pageTemp.getRelevance() / maxRelevance);
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
        int start = content.indexOf(finalTagBegin) + 7;
        int fin = content.indexOf(finalTagEnd);
        try {
            result = content.substring(start, fin);
        } catch (Exception ignored) {
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
        int start = foundedInd - common.getSnippetWords() > 0 ? foundedInd - 7 : 0;
        int fin = foundedInd + common.getSnippetWords() < words.length ? foundedInd + 7 : words.length;
        for (int i = start; i < fin; i++) {
            str += i != foundedInd ? " " + words[i] : " <b>" + words[i] + "</b>";
        }
        return new SnippetClass(str.substring(1), foundedInd);
    }

    private void addListPage(Lemma lemma) {
        List<Index> listIndex = common.getIndexRepository().findIndex(lemma.getId());
        PagesTempComparator comparator = new PagesTempComparator();
        listIndex.forEach(index -> {
            if (index.getPage().getContent().toLowerCase().contains(lemma.getLemma())) {
                PageTemp temp = new PageTemp(index.getPage());
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

    public void executeOldPage(PageTemp page, Lemma lemma, Index index) {
        page.getListLemma().add(lemma);
        page.getListIndex().add(index);
        page.setRelevance(page.getRelevance() + index.getRank());
    }

    public void executeNewPage(PageTemp page, Lemma lemma, Index index) {
        page.getListLemma().add(lemma);
        page.getListIndex().add(index);
        page.setRelevance(index.getRank());
        pages.add(page);
    }

    private long getMaxRelevance() {
        long result = 0;
        for (PageTemp pageTemp : pages) {
            if (pageTemp.getRelevance() > result) {
                result = pageTemp.getRelevance();
            }
        }
        return result;
    }
}
