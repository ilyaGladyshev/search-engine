package searchengine.tasks;

import lombok.RequiredArgsConstructor;
import searchengine.config.CommonConfiguration;
import searchengine.model.Page;

import java.io.IOException;
import java.util.TreeMap;
import java.util.concurrent.ForkJoinPool;

public class SiteIndexing extends Thread{
    private CommonConfiguration common;
    private Page page;

    public SiteIndexing(CommonConfiguration common, Page page){
        this.common = common;
        this.page = page;
    }

    @Override
    public void run() {
        super.run();
        TreeMap<String,Page> links = new TreeMap<>();
        ParseTask parseTask = new ParseTask(page, common);
        common.getListParseTasks().add(parseTask);
        TreeMap<String, Page> temp = new ForkJoinPool(Runtime.getRuntime().availableProcessors()).invoke(parseTask);
    }
}
