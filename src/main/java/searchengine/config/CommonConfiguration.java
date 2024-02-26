package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import searchengine.model.Page;
import searchengine.task.LemmaTask;
import searchengine.task.ParseTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "indexing-settings")
public class CommonConfiguration {
    private final static boolean IGNORE_HTTP_ERRORS = true;
    private final static boolean IGNORE_CONTENT_TYPE = true;
    private final static boolean FOLLOW_REDIRECTS = false;
    private final static int PAUSE_DURATION = 15000;
    private String userAgent;
    private String referrer;
    private List<Site> sites;
    private List<ParseTask> listParseTasks = new ArrayList<>();
    private Boolean isInterrupt = false;

    @Lookup
    public ParseTask createParseTask() {
        return null;
    }

    @Lookup
    public LemmaTask createLemmaTask() {
        return null;
    }

    @Bean
    public LuceneMorphology luceneMorphology() throws IOException {
        return new RussianLuceneMorphology();
    }

    public Connection getConnection(Page page) {
        return Jsoup.connect(page.getSite().getUrl() + page.getPath())
                .ignoreHttpErrors(IGNORE_HTTP_ERRORS)
                .userAgent(userAgent)
                .followRedirects(FOLLOW_REDIRECTS)
                .referrer(referrer)
                .ignoreContentType(IGNORE_CONTENT_TYPE)
                .timeout(PAUSE_DURATION);
    }
}
