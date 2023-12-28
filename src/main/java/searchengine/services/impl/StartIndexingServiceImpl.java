package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.Application;
import searchengine.config.CommonConfiguration;
import searchengine.dto.common.CommonResponse;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.StartIndexingService;
import searchengine.tasks.IndexingServiceTask;

@Service
@RequiredArgsConstructor
public class StartIndexingServiceImpl implements StartIndexingService {

    private final CommonConfiguration common;

    private final Logger logger = LogManager.getLogger(Application.class);

    @Autowired
    private final SiteRepository siteRepository;

    @Autowired
    private final PageRepository pageRepository;

    @Autowired
    private final LemmaRepository lemmaRepository;

    @Autowired
    private final IndexRepository indexRepository;

    @Override
    public CommonResponse indexing() {
        CommonResponse response = new CommonResponse();
        try {
            logger.log(Level.INFO, "Начало индексации");
            IndexingServiceTask indexingServiceTask = new IndexingServiceTask(common, siteRepository,
                    pageRepository, lemmaRepository, indexRepository);
            indexingServiceTask.start();
            response.setResult(true);
        } catch (Exception ex) {
            response.setError("Индексация не запущена");
            response.setResult(false);
        }
        return response;
    }
}
