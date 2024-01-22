package searchengine.tasks;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.Application;
import searchengine.config.CommonConfiguration;
import searchengine.model.Page;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

@Getter
@Setter
@Component
@Scope("prototype")
public class ParseTask extends RecursiveAction {

    private final int PAUSE_DURATION = 500;

    private final int MEDIUMTEXT_LENGTH = 16777215;

    private final Logger logger = LogManager.getLogger(Application.class);

    private final String CSS_QUERY = "a";

    private final String HASH_TEXT = "#";

    private final String QUESTION_TEXT = "?";

    private final char SLASH_SYMBOL = '/';

    private final char CHAR_SERVER_ERROR = '5';

    private final char CHAR_CLIENT_ERROR = '4';

    private final List<ParseTask> taskList = new ArrayList<>();

    private Page page;

    @Autowired
    private CommonConfiguration common;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageRepository pageRepository;

    private String formatReference(String ref) {
        return (!ref.contains(page.getSite().getUrl())) ? page.getSite().getUrl() + ref : ref;
    }

    private Boolean isCorrectAttr(String attr) {
        if (attr.equals(page.getSite().getUrl())) {
            return false;
        } else if (attr.contains(page.getSite().getUrl())) {
            return true;
        } else {
            return (attr.indexOf(SLASH_SYMBOL) == 0)
                    && (attr.length() > 1);
        }
    }

    private Boolean isCorrectLink(String link) {
        return (!(link.contains(HASH_TEXT))) && (!(link.contains(QUESTION_TEXT))
                && (!(link.isEmpty())));
    }

    private List<Page> findPage(String url) {
        return pageRepository.findAllByPath(url);
    }

    private void pause(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            page.getSite().renewError("Ошибка при установки паузы");
            siteRepository.save(page.getSite());
            logger.error("Ошибка при установки паузы " + e.getMessage());
        }
    }

    @Override
    protected void compute() {
        try {
            logger.log(Level.INFO, "Парсинг страницы " + page.getSite().getUrl() + page.getPath());
            pause(PAUSE_DURATION);
            Connection connection = common.getConnection(page);
            Document doc = connection.get();
            Elements elements = doc.select(CSS_QUERY);
            parsePage(elements);
        } catch (IOException ex) {
            page.getSite().renewError("Ошибка при открытии страницы");
            siteRepository.save(page.getSite());
            logger.log(Level.ERROR, "Ошибка при открытии страницы " + ex.getMessage());
        }
        if (!(common.getIsInterrupt())) {
            joinTasks(taskList);
        }
    }

    private String truncateBody(String body) {
        return (body.length() < MEDIUMTEXT_LENGTH) ? body : body.substring(0, MEDIUMTEXT_LENGTH);
    }

    private void parsePage(Elements elements) throws IOException {
        for (Element element : elements) {
            for (Attribute attr : element.attributes()) {
                if (isCorrectAttr(attr.getValue())) {
                    String ref = formatReference(attr.getValue());
                    List<Page> foundedPages = findPage(ref.substring(page.getSite().getUrl().length()));
                    if (isCorrectLink(ref) && (foundedPages.isEmpty()) && (!(common.getIsInterrupt()))) {
                        Page newPage = new Page(page.getSite(), ref);
                        Connection connection = common.getConnection(page);
                        Connection.Response response = connection.execute();
                        newPage.setCode(response.statusCode());
                        newPage.setContent(truncateBody(response.body()));
                        renewDateInDB(newPage);
                        forkTasks(newPage);
                    }
                }
            }
        }
    }

    private void forkTasks(Page newPage) {
        if (!(common.getIsInterrupt())) {
            ParseTask task = common.createParseTask();
            task.setPage(newPage);
            task.fork();
            taskList.add(task);
        }
    }

    private void joinTasks(List<ParseTask> taskList) {
        for (ParseTask task : taskList) {
            try {
                task.join();
            } catch (Exception ex) {
                page.getSite().renewError("Ошибка при вызове метода join");
                siteRepository.save(page.getSite());
                Thread.currentThread().interrupt();
                logger.error("Ошибка при вызове метода join " + ex.getMessage());
            }
        }
    }

    private Boolean isErrorPage(Page page) {
        return (Integer.toString(page.getCode()).charAt(0) == CHAR_CLIENT_ERROR)
                || (Integer.toString(page.getCode()).charAt(0) == CHAR_SERVER_ERROR);
    }

    @Transactional
    private void renewDateInDB(Page page) {
        try {
            List<Page> listOldPages = pageRepository.findAllByPath(page.getPath());
            if (!listOldPages.isEmpty()) {
                listOldPages.forEach(pageRepository::delete);
            }
            pageRepository.save(page);
            if (!(isErrorPage(page))) {
                LemmatisationTask lemmatisationTask = common.createLemmatisationTask();
                lemmatisationTask.setPage(page);
                lemmatisationTask.run();
                lemmatisationTask.saveLemmas();
            }
        } catch (Exception ex) {
            logger.error("Ошибка записи в базу " + ex.getMessage());
        }
    }


    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        for (ParseTask task : taskList) {
            task.cancel(true);
        }
        return super.cancel(mayInterruptIfRunning);
    }
}
