package searchengine.tasks;

import org.apache.logging.log4j.Level;
import searchengine.config.CommonConfiguration;
import searchengine.model.Page;
import java.util.concurrent.ForkJoinPool;

public class SiteIndexing extends Thread {

    private CommonConfiguration common;
    private Page page;

    public SiteIndexing(CommonConfiguration common, Page page) {
        this.common = common;
        this.page = page;
    }

    @Override
    public void run() {
        super.run();
        common.getLogger().log(Level.INFO, "Старт процесса индексации для сайта " + page.getSite().getUrl());
        ParseTask parseTask = new ParseTask(page, common);
        common.getListParseTasks().add(parseTask);
        new ForkJoinPool(common.getCountProcessors()).invoke(parseTask);
    }
}
