package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.CommonConfiguration;
import searchengine.dto.common.CommonResponse;
import searchengine.model.SiteModel;
import searchengine.services.StopIndexingService;

@Service
@RequiredArgsConstructor
public class StopIndexingServiceImpl implements StopIndexingService {

    private final CommonConfiguration common;
    @Override
    public CommonResponse stopIndexing() {
        CommonResponse response = new CommonResponse();
        try {
            System.out.println("Остановка индексации");
            common.setIsInterrupt(true);
            common.getListParseTasks().forEach(parseTask -> {
                parseTask.cancel(true);
                SiteModel siteModel = parseTask.getPage().getSite();
                siteModel.renewCancel();
                common.getSiteRepository().save(siteModel);
            });
            common.getListParseTasks().clear();
            response.setResult(true);
        } catch (Exception ex){
            ex.printStackTrace();
            response.setError("Ошибка остановки индексации");
            response.setResult(false);
        }

        return response;
    }
}
