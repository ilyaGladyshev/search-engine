package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import searchengine.Application;
import searchengine.model.Page;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.tasks.ParseTask;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "indexing-settings")
public class CommonConfiguration {

    private final Logger logger = LogManager.getLogger(Application.class);

    private final int countProcessors = Runtime.getRuntime().availableProcessors();

    private List<Site> sites;

    private String user_agent;

    private String referrer;

    private int lemmaFrequency;

    private int snippetWords;

    private List<ParseTask> listParseTasks = new ArrayList<>();

    private Boolean isInterrupt = false;

    @Bean
    public LuceneMorphology luceneMorphology() throws IOException {
        return new RussianLuceneMorphology();
    }

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private IndexRepository indexRepository;

    @Autowired
    private LemmaRepository lemmaRepository;

    public Connection getConnection(Page page) {
        return Jsoup.connect(page.getSite().getUrl() + page.getPath())
                .ignoreHttpErrors(true)
                .userAgent(user_agent)
                .followRedirects(false)
                .referrer(referrer)
                .ignoreContentType(true).timeout(15000).followRedirects(false);
    }

}
