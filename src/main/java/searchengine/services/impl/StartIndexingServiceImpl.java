package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;
import searchengine.Application;
import searchengine.responses.common.CommonResponse;
import searchengine.services.StartIndexingService;
import searchengine.tasks.IndexingServiceTask;

@Service
@RequiredArgsConstructor
public class StartIndexingServiceImpl implements StartIndexingService {

    private final Logger logger = LogManager.getLogger(Application.class);

    @Lookup
    public IndexingServiceTask createIndexingServiceTask() {
        return null;
    }

    @Override
    public CommonResponse indexing() {
        CommonResponse response = new CommonResponse();
        try {
            logger.log(Level.INFO, "Начало индексации");
            IndexingServiceTask indexingServiceTask = createIndexingServiceTask();
            indexingServiceTask.start();
            response.setResult(true);
        } catch (Exception ex) {
            response.setError("Индексация не запущена");
            response.setResult(false);
        }
        return response;
    }
}
