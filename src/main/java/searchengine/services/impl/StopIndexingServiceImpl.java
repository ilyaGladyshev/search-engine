package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.CommonConfiguration;
import searchengine.dto.common.CommonResponse;
import searchengine.model.SiteModel;
import searchengine.repositories.SiteRepository;
import searchengine.services.StopIndexingService;

@Service
@RequiredArgsConstructor
public class StopIndexingServiceImpl implements StopIndexingService {

    private final CommonConfiguration common;
    @Autowired
    private SiteRepository siteRepository;

    @Override
    public CommonResponse stopIndexing() {
        CommonResponse response = new CommonResponse();
        try {
            common.getLogger().log(Level.INFO, "Остановка индексации");
            common.setIsInterrupt(true);
            common.getListParseTasks().forEach(parseTask -> {
                parseTask.cancel(true);
                SiteModel siteModel = parseTask.getPage().getSite();
                siteModel.renewCancel();
                siteRepository.save(siteModel);
            });
            common.getListParseTasks().clear();
            response.setResult(true);
        } catch (Exception ex) {
            common.getLogger().log(Level.ERROR, "Ошибка остановки индексации " + ex.getMessage());
            response.setError("Ошибка остановки индексации");
            response.setResult(false);
        }

        return response;
    }
}
