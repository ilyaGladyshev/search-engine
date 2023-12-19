package searchengine.tasks;

import jakarta.transaction.Transactional;
import lombok.Getter;
import org.apache.logging.log4j.Level;
import searchengine.config.CommonConfiguration;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.services.temp.CommonLemmatization;
import java.util.*;

@Getter
public class Lemmatization extends Thread {

    private Page page;

    public HashMap<String, Integer> result = new HashMap<>();

    private CommonConfiguration common;

    @Transactional
    public void saveLemmas() {
        String strLemma;
        Lemma lemma;
        Iterator<String> iterator = result.keySet().iterator();
        while (iterator.hasNext()) {
            strLemma = iterator.next();
            List<Lemma> listLemma = common.getLemmaRepository().findAllLemmas(strLemma);
            if (listLemma.isEmpty()) {
                lemma = new Lemma(strLemma, page.getSite());
            } else {
                lemma = listLemma.get(0);
                lemma.setFrequency(lemma.getFrequency() + 1);
            }
            Index index = new Index(page, lemma, result.get(strLemma));
            common.getLemmaRepository().save(lemma);
            common.getIndexRepository().save(index);
        }
    }

    public Lemmatization(Page page, CommonConfiguration common) {
        this.page = page;
        this.common = common;
    }

    @Override
    public void run() {
        super.run();
        try {
            common.getLogger().log(Level.INFO, "Старт лемматизации для страницы " + page.getPath());
            String body = page.getContent();
            CommonLemmatization commonLemmatization = new CommonLemmatization(common.luceneMorphology());
            String rtext = commonLemmatization.getRussianText(body);
            result = commonLemmatization.executePage(rtext);
        } catch (Exception e) {
            System.out.println("Ошибка лемматизации " + e.getMessage());
            common.getLogger().log(Level.ERROR, "Ошибка лемматизации " + e.getMessage());
        }
    }
}
