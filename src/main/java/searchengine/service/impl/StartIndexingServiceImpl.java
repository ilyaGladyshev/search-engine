package searchengine.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;
import searchengine.response.common.CommonResponse;
import searchengine.service.StartIndexingService;
import searchengine.task.IndexingServiceTask;

@Service
@RequiredArgsConstructor
public class StartIndexingServiceImpl implements StartIndexingService {
    private final static Logger logger = LogManager.getLogger(StartIndexingServiceImpl.class);

    @Lookup
    public IndexingServiceTask createIndexingServiceTask() {
        return null;
    }

    @Override
    public CommonResponse indexing() {
        CommonResponse response = new CommonResponse();
        try {
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
