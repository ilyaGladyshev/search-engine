package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.springframework.stereotype.Service;
import searchengine.config.CommonConfiguration;
import searchengine.dto.common.CommonResponse;
import searchengine.services.StartIndexingService;
import searchengine.tasks.IndexingServiceTask;

@Service
@RequiredArgsConstructor
public class StartIndexingServiceImpl implements StartIndexingService {

    private final CommonConfiguration common;

    @Override
    public CommonResponse indexing() {
        CommonResponse response = new CommonResponse();
        try {
            common.getLogger().log(Level.INFO,"Начало индексации");
            IndexingServiceTask indexingService = new IndexingServiceTask(common);
            indexingService.start();
            response.setResult(true);
        } catch (Exception ex) {
            response.setError("Индексация не запущена");
            response.setResult(false);
        }
        return response;
    }
}
