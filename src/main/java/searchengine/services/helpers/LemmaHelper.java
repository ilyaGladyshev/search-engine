package searchengine.services.helpers;

import lombok.Getter;
import lombok.Setter;
import org.apache.lucene.morphology.LuceneMorphology;

import java.util.List;

@Setter
@Getter
public class LemmaHelper {
    private String word;
    private String morphInfo;
    private String form;
    private String part;

    public LemmaHelper(String word, String normal) {
        this.word = word;
        this.form = normal;
    }

    public void customization(LuceneMorphology luceneMorphology) {
        List<String> wordBaseForm = luceneMorphology.getMorphInfo(word);
        this.morphInfo = wordBaseForm.get(0);
        String[] list = morphInfo.split(" ");
        part = list[1];
    }
}
