package searchengine.tasks;

import lombok.Getter;
import searchengine.config.CommonConfiguration;
import searchengine.model.Page;
import java.util.HashMap;
@Getter
public class Lemmatization extends Thread {
    public Lemmatization(Page page, CommonConfiguration common) {
        this.page = page;
        this.common = common;
    }

    private Page page;

    private HashMap<String, Integer> result = new HashMap<>();

    private CommonConfiguration common;

    @Override
    public void run() {
        super.run();
        try{
            System.out.println("Старт лемматизации для страницы "+page.getPath());
            String body = page.getContent();
            String rtext = common.getRussianText(body);
            result = common.getListLemmas(rtext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
