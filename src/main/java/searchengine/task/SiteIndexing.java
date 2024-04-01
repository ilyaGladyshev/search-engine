package searchengine.task;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.config.CommonConfiguration;
import searchengine.model.Page;

import java.util.concurrent.ForkJoinPool;

@Component
@Setter
@Scope("prototype")
@RequiredArgsConstructor
public class SiteIndexing extends Thread {
    private final static Logger logger = LogManager.getLogger(SiteIndexing.class);
    private final static int COUNT_PROCESSORS = Runtime.getRuntime().availableProcessors();
    @Autowired
    private final CommonConfiguration common;
    private Page page;

    @Override
    public void run() {
        logger.log(Level.INFO, "Старт процесса индексации для сайта " + page.getSite().getUrl());
        ParseTask parseTask = common.createParseTask();
        parseTask.setPage(page);
        common.getListParseTasks().add(parseTask);
        try {
            new ForkJoinPool(COUNT_PROCESSORS).invoke(parseTask);
        } catch (Exception ex){
            logger.log(Level.ERROR, "Не удалось запустить процесс индексации для страницы " + page.getSite().getUrl());
        }
    }
}
