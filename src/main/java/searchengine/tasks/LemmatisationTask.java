package searchengine.tasks;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.Application;
import searchengine.config.CommonConfiguration;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.services.helpers.CommonLemmatisationHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Map;

@Getter
@Setter
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class LemmatisationTask extends Thread {
    public Map<String, Integer> result = new HashMap<>();
    private final Logger logger = LogManager.getLogger(Application.class);
    @Autowired
    private final CommonConfiguration common;
    @Autowired
    private final LemmaRepository lemmaRepository;
    @Autowired
    private final IndexRepository indexRepository;
    private Page page;

    @Transactional
    public void saveLemmas() {
        String strLemma;
        Lemma lemma;
        Iterator<String> iterator = result.keySet().iterator();
        while (iterator.hasNext()) {
            strLemma = iterator.next();
            List<Lemma> listLemma = lemmaRepository.findAllByLemma(strLemma);
            if (listLemma.isEmpty()) {
                lemma = new Lemma(strLemma, page.getSite());
            } else {
                lemma = listLemma.get(0);
                lemma.setFrequency(lemma.getFrequency() + 1);
            }
            Index index = new Index(page, lemma, result.get(strLemma));
            lemmaRepository.save(lemma);
            indexRepository.save(index);
        }
    }

    @Override
    public void run() {
        try {
            logger.log(Level.INFO, "Старт лемматизации для страницы " + page.getPath());
            String body = page.getContent();
            CommonLemmatisationHelper commonLemmatisationHelper = new CommonLemmatisationHelper(common.luceneMorphology());
            String russianText = commonLemmatisationHelper.getRussianText(body);
            result = commonLemmatisationHelper.getLemmasByPageText(russianText);
        } catch (Exception e) {
            System.out.println("Ошибка лемматизации " + e.getMessage());
            logger.log(Level.ERROR, "Ошибка лемматизации " + e.getMessage());
        }
    }
}
