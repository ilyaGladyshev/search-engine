package searchengine.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.CommonConfiguration;
import searchengine.dto.common.CommonResponce;
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
        System.out.println(path);
        List<Page> listFinded = common.getPageRepository().findPage(path);
        if (listFinded.size() != 0){
            Page oldPage = listFinded.get(0);
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
    public CommonResponce add(String url) {
        url = decodeUrl(url);
        CommonResponce responce = new CommonResponce();
        String siteUrl = checkPage(url);
        System.out.println(siteUrl);
        if (!(siteUrl.equals(""))) {
            try {
                SiteModel siteModel = getSiteModel(siteUrl);
                Page page = getPage(siteModel, url);
                Lemmatization lemmatization = new Lemmatization(page, common);
                lemmatization.run();
                common.saveLemmas(lemmatization.getResult(), page);
                responce.setResult(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                responce.setResult(false);
                responce.setError("Не удалось проиндексировать страницу");
            }
        } else{
            responce.setResult(false);
            responce.setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
        return responce;
    }
}
