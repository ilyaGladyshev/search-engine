package searchengine.task;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.morphology.LuceneMorphology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.config.CommonConfiguration;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.service.helper.CommonLemmaHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class LemmaTask extends Thread {
    public Map<String, Integer> result = new HashMap<>();
    private final static Logger logger = LogManager.getLogger(LemmaTask.class);
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
        for (String s : result.keySet()) {
            strLemma = s;
            List<Lemma> listLemma = lemmaRepository.findAllByLemmaAndSite_id(strLemma, page.getSite().getId());
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
        result.clear();
    }

    @Override
    public void run() {
        try {
            logger.log(Level.INFO, "Старт лемматизации для страницы " + page.getPath()+" сайта " + page.getSite().getUrl());
            String body = page.getContent();
            LuceneMorphology luceneMorphology = common.luceneMorphology();
            CommonLemmaHelper commonLemmaHelper = new CommonLemmaHelper(luceneMorphology);
            String russianText = commonLemmaHelper.getRussianText(body);
            result = commonLemmaHelper.getLemmasByPageText(russianText);
        } catch (Exception e) {
            System.out.println("Ошибка лемматизации " + e.getMessage());
            logger.log(Level.ERROR, "Ошибка лемматизации " + e.getMessage());
        }
    }
}
