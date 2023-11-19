package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.CommonConfiguration;
import searchengine.dto.common.CommonResponce;
import searchengine.model.SiteModel;
import searchengine.services.StopIndexingService;

@Service
@RequiredArgsConstructor
public class StopIndexingServiceImpl implements StopIndexingService {

    private final CommonConfiguration common;
    @Override
    public CommonResponce stopIndexing() {
        CommonResponce responce = new CommonResponce();
        try {
            common.setIsInterrapt(true);
            common.getListParseTasks().forEach(parseTask -> {
                parseTask.cancel(true);
                SiteModel siteModel = parseTask.getPage().getSite();
                siteModel.renewCancel();
                common.getSiteRepository().save(siteModel);
            });
            common.getListParseTasks().clear();
            responce.setResult(true);
        } catch (Exception ex){
            ex.printStackTrace();
            responce.setError("Ошибка остановки индексации");
            responce.setResult(false);
        }

        return responce;
    }
}
