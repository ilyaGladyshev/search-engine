package searchengine.tasks;

import jakarta.transaction.Transactional;
import lombok.Getter;
import org.jsoup.Connection;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.config.CommonConfiguration;
import searchengine.model.Page;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.RecursiveTask;

@Getter
public class ParseTask extends RecursiveTask<TreeMap<String, Page>> {

    private Page page;

    private List<ParseTask> taskList = new ArrayList<>();

    private TreeMap<String, Page> links = new TreeMap<String, Page>();

    private CommonConfiguration common;

    public ParseTask(Page page, CommonConfiguration common) {
        this.page = page;
        this.common = common;
    }

    private String formatReference(String ref){
        ref = (ref.contains(page.getSite().getUrl())==false) ? page.getSite().getUrl()+ref : ref;
        return ref;
    }
    private Boolean isCorrectAttr(String attr){
        if(attr.contains(page.getSite().getUrl())){
            return true;
        } else if ((attr.indexOf('/')==0)
                &&(attr.length()>1)){
            return true;
        } else {
            return false;
        }
    }
    private Boolean isCorrectLink(String link){
        return ((!(link.contains("#")))&&(!(link.contains("?")))) ? true : false;
    }

    //@Transactional
    private List<Page> findPage(String url){
        return common.getPageRepository().findPage(url);
    }

    private void pause(int duration){
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            page.getSite().renewError("Ошибка при установки паузы");
            common.getSiteRepository().save(page.getSite());
            e.printStackTrace();
        }
    }

    @Override
    protected TreeMap<String, Page> compute() {
        try {
            System.out.println("Парсинг страницы "+page.getPath());
            pause(500);
            Connection connection = common.getConnection(page);
            Document doc = connection.get();
            Elements elements = doc.select("a");
            for (Element element : elements) {
                for (Attribute attr : element.attributes()) {
                    if (isCorrectAttr(attr.getValue())) {
                        String ref = formatReference(attr.getValue());
                        List<Page> foundedPages = findPage(ref.substring(page.getSite().getUrl().length()));
                        if (isCorrectLink(ref) && (foundedPages.size() == 0) && (!(common.getIsInterrupt()))) {
                            Page newPage = new Page(page.getSite(), ref, common);
                            renewDateInDB(newPage);
                            forkTasks(ref, newPage);
                        }
                    }
                };
            };
        } catch (IOException ex) {
            page.getSite().renewError("Ошибка при открытии страницы");
            common.getSiteRepository().save(page.getSite());
            ex.printStackTrace();
        }
        if (!(common.getIsInterrupt())) {
            joinTasks(taskList);
        }
        return links;
    }

    private void forkTasks(String ref, Page newPage){
        ParseTask task = new ParseTask(newPage, common);
        task.fork();
        taskList.add(task);
        links.put(ref, newPage);
    }

    private void joinTasks(List<ParseTask> taskList){
        for (ParseTask task : taskList) {
            try {
                TreeMap<String, Page> temp = task.join();
                temp.values().forEach(t -> {
                    List<Page> foundedPages = common.getPageRepository().findPage((t.getPath()));
                    if (foundedPages.size() == 0) {
                        links.put(t.getPath(), t);
                    }
                });
            } catch (Exception ex) {
                page.getSite().renewError("Ошибка при вызове метода join");
                common.getSiteRepository().save(page.getSite());
                Thread.currentThread().interrupt();
                ex.printStackTrace();
            };
        }
    }

    @Transactional
    private void renewDateInDB(Page page) throws IOException {
        common.getPageRepository().save(page);
        page.getSite().renew();
        common.getSiteRepository().save(page.getSite());
        if (!((Integer.toString(page.getCode()).substring(0,1)=="4") || (Integer.toString(page.getCode()).substring(0,1)=="5"))){
            Lemmatization lemmatization = new Lemmatization(page, common);
            lemmatization.run();
            common.saveLemmas(lemmatization.getResult(),page);
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
