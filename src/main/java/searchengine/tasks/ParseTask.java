package searchengine.tasks;

import jakarta.transaction.Transactional;
import lombok.Getter;
import org.apache.logging.log4j.Level;
import org.jsoup.Connection;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.config.CommonConfiguration;
import searchengine.model.Page;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.RecursiveAction;

@Getter
public class ParseTask extends RecursiveAction {

    private final int pauseDuration = 500;

    private final String cssQuery = "a";

    private final String hashText = "#";

    private final String questionText = "?";

    private final char slashSymbol = '/';

    private final char charServerError = '5';

    private final char charClientError = '4';

    private Page page;

    private List<ParseTask> taskList = new ArrayList<>();

    private TreeMap<String, Page> links = new TreeMap<>();

    private CommonConfiguration common;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageRepository pageRepository;

    public ParseTask(Page page, CommonConfiguration common) {
        this.page = page;
        this.common = common;
    }

    private String formatReference(String ref) {
        ref = (!ref.contains(page.getSite().getUrl())) ? page.getSite().getUrl() + ref : ref;
        return ref;
    }

    private Boolean isCorrectAttr(String attr) {
        if (attr.contains(page.getSite().getUrl())) {
            return true;
        } else return (attr.indexOf(slashSymbol) == 0)
                && (attr.length() > 1);
    }

    private Boolean isCorrectLink(String link) {
        return (!(link.contains(hashText))) && (!(link.contains(questionText)));
    }

    @Transactional
    private List<Page> findPage(String url) {
        return pageRepository.findPage(url);
    }

    private void pause(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            page.getSite().renewError("Ошибка при установки паузы");
            siteRepository.save(page.getSite());
            common.getLogger().error("Ошибка при установки паузы " + e.getMessage());
        }
    }

    @Override
    protected void compute() {
        try {
            common.getLogger().log(Level.INFO, "Парсинг страницы " + page.getSite().getUrl() + page.getPath());
            pause(pauseDuration);
            Connection connection = common.getConnection(page);
            Document doc = connection.get();
            Elements elements = doc.select(cssQuery);
            parsePage(elements);
        } catch (IOException ex) {
            page.getSite().renewError("Ошибка при открытии страницы");
            siteRepository.save(page.getSite());
            common.getLogger().log(Level.ERROR, "Ошибка при открытии страницы " + ex.getMessage());
        }
        if (!(common.getIsInterrupt())) {
            joinTasks(taskList);
        }
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
                        newPage.setContent(response.body());
                        renewDateInDB(newPage);
                        forkTasks(ref, newPage);
                    }
                }
            }
        }
    }

    private void forkTasks(String ref, Page newPage) {
        if (!(common.getIsInterrupt())) {
            ParseTask task = new ParseTask(newPage, common);
            task.fork();
            taskList.add(task);
            links.put(ref, newPage);
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
                common.getLogger().error("Ошибка при вызове метода join " + ex.getMessage());
            }
        }
    }

    @Transactional
    private void renewDateInDB(Page page) {
        pageRepository.save(page);
        if (!((Integer.toString(page.getCode()).charAt(0) == charClientError) || (Integer.toString(page.getCode()).charAt(0) == charServerError))) {
            Lemmatization lemmatization = new Lemmatization(page, common);
            lemmatization.run();
            System.out.println("Lemmatization size " + lemmatization.result.size());
            lemmatization.saveLemmas();
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
