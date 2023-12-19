package searchengine.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.CommonConfiguration;
import searchengine.dto.common.CommonResponse;
import searchengine.config.Site;
import searchengine.model.Page;
import searchengine.model.SiteModel;
import searchengine.services.AddIndexingPageService;
import searchengine.tasks.Lemmatization;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddIndexingPageServiceImpl implements AddIndexingPageService {

    private final CommonConfiguration common;

    private String decodeUrl(String url){
        String result = "";
        try {
            result = java.net.URLDecoder.decode(url.substring(url.indexOf('=')+1), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String checkPage(String url){
        String result = "";
        for(Site s: common.getSites()) {
            if (url.contains(s.getUrl())){
                result = s.getUrl();
            }
        };
        return result;
    }
    @Transactional
    private Page getPage(SiteModel site, String url) throws IOException {
        String path = url.substring(site.getUrl().length());
        List<Page> listFounded = common.getPageRepository().findPage(path);
        if (listFounded.size() != 0){
            Page oldPage = listFounded.get(0);
            common.getPageRepository().delete(oldPage);
        }
        Page page = new Page(site, url, common);
        common.getPageRepository().save(page);
        return page;
    }
    @Transactional
    private SiteModel getSiteModel(String url){
        List<SiteModel> listFounded = common.getSiteRepository().findSiteByUrl(url);
        if (listFounded.size() == 0){
            SiteModel site = new SiteModel(url);
            common.getSiteRepository().save(site);
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
        System.out.println("Добавление страницы "+url+" в очередь на индексацию");
        if (!(siteUrl.equals(""))) {
            try {
                SiteModel siteModel = getSiteModel(siteUrl);
                Page page = getPage(siteModel, url);
                Lemmatization lemmatization = new Lemmatization(page, common);
                lemmatization.run();
                common.saveLemmas(lemmatization.getResult(), page);
                response.setResult(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                response.setResult(false);
                response.setError("Не удалось проиндексировать страницу");
            }
        } else{
            response.setResult(false);
            response.setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
        return response;
    }
}
