package searchengine.tasks;

import lombok.Getter;
import org.apache.lucene.morphology.LuceneMorphology;
import searchengine.config.CommonConfiguration;
import searchengine.model.LemmaTemp;
import searchengine.model.Page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class Lemmatization extends Thread {
    public Lemmatization(Page page, CommonConfiguration common) {
        this.page = page;
        this.common = common;
    }

    private Page page;

    private HashMap<String, Integer> result = new HashMap<>();

    private CommonConfiguration common;

    @Override
    public void run() {
        super.run();
        try{
            String body = page.getContent();
            String rtext = common.getRussianText(body);
            result = common.getListLemmas(rtext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
