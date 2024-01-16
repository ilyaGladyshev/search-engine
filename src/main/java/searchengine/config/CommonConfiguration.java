package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Component;
import searchengine.model.Page;
import searchengine.tasks.ParseTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "indexing-settings")
public class CommonConfiguration {

    private final boolean IGNORE_HTTP_ERRORS = true;

    private final boolean IGNORE_CONTENT_TYPE = true;

    private final boolean FOLLOW_REDIRECTS = false;

    private final int COUNT_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private List<Site> sites;

    private String USER_AGENT;

    private String REFERRER;

    private int LEMMA_FREQUENCY;

    private int SNIPPET_WORDS;

    private int PAUSE_DURATION;

    private List<ParseTask> listParseTasks = new ArrayList<>();

    private Boolean isInterrupt = false;

    @Bean
    public ApplicationContextProvider contextProvider() {
        return new ApplicationContextProvider();
    }

    @Bean
    public LuceneMorphology luceneMorphology() throws IOException {
        return new RussianLuceneMorphology();
    }

    public Connection getConnection(Page page) {
        return Jsoup.connect(page.getSite().getUrl() + page.getPath())
                .ignoreHttpErrors(IGNORE_HTTP_ERRORS)
                .userAgent(USER_AGENT)
                .followRedirects(FOLLOW_REDIRECTS)
                .referrer(REFERRER)
                .ignoreContentType(IGNORE_CONTENT_TYPE)
                .timeout(PAUSE_DURATION);
    }

}
