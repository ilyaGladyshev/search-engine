package searchengine.services.temp;

import org.apache.lucene.morphology.LuceneMorphology;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonLemmatization {

    private final String russianRegExp = "[а-яА-Я ]";

    public LuceneMorphology luceneMorphology;
    public CommonLemmatization(LuceneMorphology luceneMorphology) {
        this.luceneMorphology = luceneMorphology;
    }

    public HashMap<String, Integer> executePage(String rtext) throws IOException {
        HashMap<String, Integer> tempResult = new HashMap<>();
        String[] listWords = rtext.split(" ");
        for (String word : listWords) {
            if (!(word.isEmpty())){
                List<LemmaTemp> lemmaList = getLemmalist(word.trim().toLowerCase(), luceneMorphology);
                if (checkWord(lemmaList.get(0).getPart())) {
                    lemmaList.forEach(l->{
                        if (tempResult.get(l.getForm()) == null) {
                            tempResult.put(l.getForm(), 1);
                        } else {
                            int count = tempResult.get(l.getForm());
                            tempResult.replace(l.getForm(), count, count+1);
                        }
                    });
                }
            }
        }
        return tempResult;
    }

    private Boolean checkWord(String inf) {

        return !((inf.equals("СОЮЗ")) || (inf.equals("МЕЖД")) || (inf.equals("ЧАСТ"))
                || (inf.equals("ПРЕДЛ")) || (inf.equals("МС")));
    }

    private List<LemmaTemp> getLemmalist(String word, LuceneMorphology luceneMorphology) {
        List<LemmaTemp> result = new ArrayList<>();
        List<String> listNormal = luceneMorphology.getNormalForms(word);
        listNormal.forEach(n -> {
            result.add(new LemmaTemp(word, n, luceneMorphology));
        });
        return result;
    }

    public String getRussianText(String body) {
        Pattern pattern = Pattern.compile(russianRegExp);
        Matcher matcher = pattern.matcher(body);
        StringBuilder rtext = new StringBuilder();
        while (matcher.find()) {
            rtext.append(matcher.group(0));
        }
        return rtext.toString();
    }
}
