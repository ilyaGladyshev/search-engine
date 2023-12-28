package searchengine.tasks;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.Application;
import searchengine.config.CommonConfiguration;
import searchengine.model.Page;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.concurrent.ForkJoinPool;

@Component
@Setter
@RequiredArgsConstructor
public class SiteIndexing extends Thread {

    private final Logger logger = LogManager.getLogger(Application.class);

    private final Page page;

    @Autowired
    private final CommonConfiguration common;

    @Autowired
    private final SiteRepository siteRepository;

    @Autowired
    private final PageRepository pageRepository;

    @Autowired
    private final LemmaRepository lemmaRepository;

    @Autowired
    private final IndexRepository indexRepository;

    @Override
    public void run() {
        logger.log(Level.INFO, "Старт процесса индексации для сайта " + page.getSite().getUrl());
        ParseTask parseTask = new ParseTask(page, common, siteRepository,
                pageRepository, indexRepository, lemmaRepository);
        common.getListParseTasks().add(parseTask);
        new ForkJoinPool(common.getCOUNT_PROCESSORS()).invoke(parseTask);
    }
}
