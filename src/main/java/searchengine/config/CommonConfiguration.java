package searchengine.config;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.LemmaTemp;
import searchengine.model.Page;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.tasks.ParseTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "indexing-settings")
public class CommonConfiguration {
    private List<Site> sites;

    private  String user_agent;

    private String referrer;

    private int lemmaFrequency;

    private int snippetWords;

    private List<ParseTask> listParseTasks = new ArrayList<ParseTask>();

    private Boolean isInterrupt = false;

    public Connection getConnection(Page page){
        return Jsoup.connect(page.getSite().getUrl()+page.getPath())
                .ignoreHttpErrors(true)
                .userAgent(user_agent)
                .followRedirects(false)
                .referrer(referrer)
                .ignoreContentType(true).timeout(15000).followRedirects(false);
    }

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private IndexRepository indexRepository;

    @Autowired
    private LemmaRepository lemmaRepository;

    @Bean
    public LuceneMorphology luceneMorphology() throws IOException {
        return new RussianLuceneMorphology();
    }
    public static String formatUrl(String url){
        String result = "";
        if (url.substring(0,1) == "/") {
            url = url.substring(1);
        }
        result = (url.contains("/")) ? url.substring(0, url.indexOf("/")) : url;
        return result;
    }

    @Transactional
    public void saveLemmas(HashMap<String, Integer> lemmas, Page page){
        String strLemma = "";
        Lemma lemma = null;
        Iterator<String> iterator = lemmas.keySet().iterator();
        while (iterator.hasNext()){
            strLemma = iterator.next();
            List<Lemma> listLemma = lemmaRepository.findAllLemmas(strLemma);
            if (listLemma.size()==0){
                lemma = new Lemma(strLemma, page.getSite());
            } else {
                lemma = listLemma.get(0);
                lemma.setFrequency(lemma.getFrequency() + 1);
            }
            Index index = new Index(page, lemma, lemmas.get(strLemma).intValue());
            lemmaRepository.save(lemma);
            indexRepository.save(index);
        }
    }

    private Boolean checkWord(String inf){
        Boolean result = (!((inf.equals("СОЮЗ")) || (inf.equals("МЕЖД") ) || (inf.equals("ЧАСТ"))
                || (inf.equals("ПРЕДЛ")) || (inf.equals("МС")) )) ? true: false;
        return result;
    }

    private List<LemmaTemp> getLemmalist(String word, LuceneMorphology luceneMorphology){
        List<LemmaTemp> result = new ArrayList<>();
        List<String> listNormal = luceneMorphology.getNormalForms(word);
        listNormal.forEach(n ->{
            result.add(new LemmaTemp(word, n, luceneMorphology));
        });
        return result;
    }

    public HashMap<String, Integer> getListLemmas(String rtext) throws IOException {
        HashMap<String, Integer> result = new HashMap<>();
        String[] listWords = rtext.split(" ");
        for (String word : listWords) {
            if (word!=""){
                List<LemmaTemp> lemmaList = getLemmalist(word.trim().toLowerCase(), this.luceneMorphology());//new LemmaTemp(word.trim().toLowerCase(), luceneMorphology);
                if (checkWord(lemmaList.get(0).getPart())) {
                    lemmaList.forEach(l->{
                        if (result.get(l.getForm()) == null) {
                            result.put(l.getForm(), 1);
                        } else {
                            int count = result.get(l.getForm()).intValue();
                            result.replace(l.getForm(), count, count+1);
                        }
                    });
                }
            }
        }
        return result;
    }

    public String getTitle(String content){
        String result = "";
        int start = content.indexOf("<title>")+7;
        int fin = content.indexOf("</title>");
        try {
            result = content.substring(start, fin);
        } catch(Exception ex){
        }
        return result;
    }
    public String getRussianText(String body ){
        Pattern pattern = Pattern.compile("[а-яА-Я ]");
        Matcher matcher = pattern.matcher(body);
        String rtext = "";
        while (matcher.find()) {
            rtext+= matcher.group(0);
        }
        return rtext;
    }
}
