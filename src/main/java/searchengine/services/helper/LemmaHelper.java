package searchengine.services.helper;

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

    public LemmaHelper(String word, String normal, LuceneMorphology luceneMorphology) {
        this.word = word;
        List<String> wordBaseForm = luceneMorphology.getMorphInfo(word);
        this.morphInfo = wordBaseForm.get(0);
        String[] list = morphInfo.split(" ");
        this.form = normal;
        this.part = list[1];
    }
}
