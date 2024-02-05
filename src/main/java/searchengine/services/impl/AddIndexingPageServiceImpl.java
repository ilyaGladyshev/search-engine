package searchengine.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.Application;
import searchengine.config.CommonConfiguration;
import searchengine.responses.common.CommonResponse;
import searchengine.config.Site;
import searchengine.model.Page;
import searchengine.model.SiteModel;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.AddIndexingPageService;
import searchengine.tasks.LemmatisationTask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.net.URLDecoder.decode;

@Service
@RequiredArgsConstructor
public class AddIndexingPageServiceImpl implements AddIndexingPageService {
    private final CommonConfiguration common;
    private final Logger logger = LogManager.getLogger(Application.class);
    @Autowired
    private final PageRepository pageRepository;
    @Autowired
    private final SiteRepository siteRepository;

    private String decodeUrl(String url) {
        return decode(url.substring(url.indexOf('=') + 1), StandardCharsets.UTF_8);
    }

    private String checkPageByRootSite(String url) {
        String result = "";
        for (Site s : common.getSites()) {
            if (url.contains(s.getUrl())) {
                result = s.getUrl();
            }
        }
        return result;
    }

    @Transactional
    private Page getPage(SiteModel site, String url) throws IOException {
        String path = url.substring(site.getUrl().length());
        List<Page> listFounded = pageRepository.findAllByPath(path);
        if (!listFounded.isEmpty()) {
            Page oldPage = listFounded.get(0);
            pageRepository.delete(oldPage);
        }
        Page page = new Page(site, url);
        Connection connection = common.getConnection(page);
        Connection.Response response = connection.execute();
        page.setCode(response.statusCode());
        page.setContent(response.body());
        pageRepository.save(page);
        return page;
    }

    public static String formatUrl(String url) {
        String result;
        if (url.charAt(0) == '/') {
            url = url.substring(1);
        }
        result = (url.contains("/")) ? url.substring(0, url.indexOf("/")) : url;
        return result;
    }

    @Transactional
    private SiteModel getSiteModel(String url) {
        List<SiteModel> listFounded = siteRepository.findAllByUrl(url);
        if (listFounded.isEmpty()) {
            SiteModel site = new SiteModel(formatUrl(url));
            siteRepository.save(site);
            return site;
        } else {
            return listFounded.get(0);
        }
    }

    @Override
    public CommonResponse add(String url) {
        url = decodeUrl(url);
        CommonResponse response = new CommonResponse();
        String siteUrl = checkPageByRootSite(url);
        logger.log(Level.INFO, "Добавление страницы " + url + " в очередь на индексацию");
        if (!(siteUrl.isEmpty())) {
            try {
                lemmatisation(siteUrl, response, url);
            } catch (Exception ex) {
                logger.log(Level.ERROR, ex.getMessage());
                response.setResult(false);
                response.setError("Не удалось проиндексировать страницу");
            }
        } else {
            response.setResult(false);
            response.setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
        return response;
    }

    public void lemmatisation(String siteUrl, CommonResponse response, String url) throws Exception {
        SiteModel siteModel = getSiteModel(siteUrl);
        Page page = getPage(siteModel, url);
        LemmatisationTask lemmatisationTask = common.createLemmatisationTask();
        lemmatisationTask.setPage(page);
        lemmatisationTask.start();
        lemmatisationTask.saveLemmas();
        response.setResult(true);
    }
}
