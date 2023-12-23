package searchengine.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.CommonConfiguration;
import searchengine.dto.common.CommonResponse;
import searchengine.config.Site;
import searchengine.model.Page;
import searchengine.model.SiteModel;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.AddIndexingPageService;
import searchengine.tasks.Lemmatization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddIndexingPageServiceImpl implements AddIndexingPageService {

    private final CommonConfiguration common;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private SiteRepository siteRepository;

    private String decodeUrl(String url) {
        return java.net.URLDecoder.decode(url.substring(url.indexOf('=') + 1), StandardCharsets.UTF_8);
    }

    private String checkPage(String url) {
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
        List<Page> listFounded = pageRepository.findPage(path);
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
        List<SiteModel> listFounded = siteRepository.findSiteByUrl(url);
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
        String siteUrl = checkPage(url);
        common.getLogger().log(Level.INFO, "Добавление страницы " + url + " в очередь на индексацию");
        if (!(siteUrl.isEmpty())) {
            try {
                lemmatization(siteUrl, response, url);
            } catch (Exception ex) {
                common.getLogger().log(Level.ERROR, ex.getMessage());
                ;
                response.setResult(false);
                response.setError("Не удалось проиндексировать страницу");
            }
        } else {
            response.setResult(false);
            response.setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
        return response;
    }

    public void lemmatization(String siteUrl, CommonResponse response, String url) throws Exception {
        SiteModel siteModel = getSiteModel(siteUrl);
        Page page = getPage(siteModel, url);
        Lemmatization lemmatization = new Lemmatization(page, common);
        lemmatization.start();
        lemmatization.saveLemmas();
        response.setResult(true);
    }
}
