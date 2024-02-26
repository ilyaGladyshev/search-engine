package searchengine.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.CommonConfiguration;
import searchengine.response.common.CommonResponse;
import searchengine.model.SiteModel;
import searchengine.repository.SiteRepository;
import searchengine.service.StopIndexingService;

@Service
@RequiredArgsConstructor
public class StopIndexingServiceImpl implements StopIndexingService {
    private final static Logger logger = LogManager.getLogger(StopIndexingServiceImpl.class);
    private final CommonConfiguration common;
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
