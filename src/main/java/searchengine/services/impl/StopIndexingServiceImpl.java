package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.CommonConfiguration;
import searchengine.responses.common.CommonResponse;
import searchengine.model.SiteModel;
import searchengine.repositories.SiteRepository;
import searchengine.services.StopIndexingService;

@Service
@RequiredArgsConstructor
public class StopIndexingServiceImpl implements StopIndexingService {
    private final CommonConfiguration common;
    private final Logger logger = LogManager.getLogger();
    @Autowired
    private final SiteRepository siteRepository;

    @Override
    public CommonResponse stopIndexing() {
        CommonResponse response = new CommonResponse();
        try {
            logger.log(Level.INFO, "Остановка индексации");
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
            logger.log(Level.ERROR, "Ошибка остановки индексации " + ex.getMessage());
            response.setError("Ошибка остановки индексации");
            response.setResult(false);
        }
        return response;
    }
}
