package searchengine.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;
import searchengine.response.common.CommonResponse;
import searchengine.service.StartIndexingService;
import searchengine.task.IndexingServiceTask;

@Service
@RequiredArgsConstructor
public class StartIndexingServiceImpl implements StartIndexingService {

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
