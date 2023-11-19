package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.CommonConfiguration;
import searchengine.dto.searching.SearchingData;
import searchengine.dto.searching.SearchingDataComparator;
import searchengine.dto.searching.SearchingResponce;
import searchengine.model.*;
import searchengine.services.SearchingService;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchingServiceImpl implements SearchingService {

    private final CommonConfiguration common;
    private List<PageTemp> pages = new ArrayList<>();
    @Override
    public SearchingResponce searching(String query, int offset,
                                       int limit, String site) {
        SearchingResponce searchingResponce = new SearchingResponce();
        try {
            HashMap<String, Integer> listLemmas = common.getListLemmas(query);
            List<Lemma> listLemmaModel = getListLemmaModel(listLemmas, site);
            searchingResponce = getSearchResponce(listLemmaModel);
        } catch (Exception e) {
            searchingResponce.setResult(false);
            searchingResponce.setError("Задан пустой поисковый запрос");
        }
        return searchingResponce;
    }

    private List<Lemma> getListLemmaModel(HashMap<String, Integer> listLemmas, String site){
        List<Lemma> resultList = new ArrayList<>();
        listLemmas.keySet().forEach(l -> {
            List<Lemma> tempList = new ArrayList<>();
            if (site == null){
                tempList = common.getLemmaRepository().findAllLemmas(l);
            } else {
                SiteModel s = common.getSiteRepository().findSiteByUrl(site).get(0);
                tempList = common.getLemmaRepository().findLemmaBySite(l, Integer.toString(s.getId()));
            }
            tempList.forEach(t ->{
                if (t.getFrequency()< common.getLemmaFrequency()){
                    resultList.add(t);
                }
            });
        });
       return resultList;
    }

    private SearchingResponce getSearchResponce(List<Lemma> lemmaList){
        SearchingResponce searchingResponce = new SearchingResponce();
        searchingResponce.setResult(true);
        searchingResponce.setData(new ArrayList<>());
        LemmaComparator comparator = new LemmaComparator();
        Collections.sort(lemmaList, comparator);
        int count = 0;
        lemmaList.forEach(l -> addListPage(l));
        long maxRelevance = getMaxRelevance();
        for (PageTemp pageTemp:pages){
            String content = common.getRussianText(pageTemp.getPage().getContent());
            String[] words = content.split(" ");
            for (Index i:pageTemp.getListIndex()){
                SnippetClass snippet = getSnippet(words, i.getLemma().getLemma());
                System.out.println(pageTemp.getRelevance()+" "+maxRelevance+" "+(double)pageTemp.getRelevance()/maxRelevance);
                SearchingData data = new SearchingData(i, common,
                        snippet.getText(), (double)pageTemp.getRelevance()/maxRelevance);
                searchingResponce.getData().add(data);
                words = Arrays.copyOfRange(words, snippet.getIndex(), words.length);
                count++;
            };
        };
        searchingResponce.setCount(count);
        SearchingDataComparator dataComparator = new SearchingDataComparator();
        Collections.sort(searchingResponce.getData(), dataComparator);
        pages.clear();
        return searchingResponce;
    }

    private SnippetClass getSnippet(String[] words, String text){
        String str = "";
        int index = 0;
        int foundedInd = 0;
        for (String word:words){
            if (word.toLowerCase().contains(text)){
                foundedInd = index;
            }
            index++;
        }
        int start = foundedInd - common.getSnippetWords() > 0 ? foundedInd - 7 : 0;
        int fin = foundedInd + common.getSnippetWords() < words.length ? foundedInd + 7 : words.length;
        for(int i = start; i<fin; i++){
            str+= i!= foundedInd ? " "+words[i] : " <b>"+words[i]+"</b>";
        }
        return new SnippetClass(str.substring(1), foundedInd);
    }

    private void addListPage(Lemma lemma){
        List<PageTemp> result = new ArrayList<>();
        List<Index> listIndex = common.getIndexRepository().findIndex(lemma.getId());
        PagesTempComparator comparator = new PagesTempComparator();
        listIndex.forEach(index ->{
            if (index.getPage().getContent().toLowerCase().contains(lemma.getLemma())){
                PageTemp temp = new PageTemp(index.getPage());
                Collections.sort(pages, comparator);
                int i = Collections.binarySearch(pages, temp, comparator);
                if (i >= 0){
                    PageTemp page = pages.get(i);
                    page.getListLemma().add(lemma);
                    page.getListIndex().add(index);
                    page.setRelevance(page.getRelevance() + index.getRank());
                    System.out.println("renew "+page.getPage().getPath()+" "+index.getLemma().getLemma()+page.getRelevance());
                } else {
                    temp.getListLemma().add(lemma);
                    temp.getListIndex().add(index);
                    temp.setRelevance(index.getRank());
                    pages.add(temp);
                    System.out.println("add "+temp.getPage().getPath()+" "+index.getLemma().getLemma()+temp.getRelevance());
                }
            }
        });
    }
    private long getMaxRelevance(){
        long result = 0;
        for(PageTemp pageTemp:pages) {
            if (pageTemp.getRelevance()>result){
                result = pageTemp.getRelevance();
            }
        };
        return result;
    }
}
