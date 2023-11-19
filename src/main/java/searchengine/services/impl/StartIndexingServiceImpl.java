package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.CommonConfiguration;
import searchengine.dto.common.CommonResponce;
import searchengine.services.StartIndexingService;
import searchengine.tasks.IndexingServiceTask;

@Service
@RequiredArgsConstructor
public class StartIndexingServiceImpl implements StartIndexingService {

    private final CommonConfiguration common;

    @Override
    public CommonResponce indexing() {
        CommonResponce responce = new CommonResponce();
        try {
            IndexingServiceTask indexingService = new IndexingServiceTask(common);
            indexingService.start();
            responce.setResult(true);
        } catch (Exception ex){
            responce.setError("Индексация не запущена");
            responce.setResult(false);
        }
        return responce;
    }
}
