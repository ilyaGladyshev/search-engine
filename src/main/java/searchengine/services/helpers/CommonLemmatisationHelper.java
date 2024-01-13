package searchengine.services.helpers;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class CommonLemmatisationHelper {

    private final String RUSSIAN_REG_EXP = "[а-яА-Я ]";

    private final LuceneMorphology luceneMorphology;

    public Map<String, Integer> getLemmasByPageText(String russianText) {
        Map<String, Integer> tempResult = new HashMap<>();
        String[] listWords = russianText.split(" ");
        for (String word : listWords) {
            if (!(word.isEmpty())) {
                lemmatisateWord(tempResult, word);
            }
        }
        return tempResult;
    }

    private void lemmatisateWord(Map<String, Integer> tempResult, String word) {
        List<LemmaHelper> lemmaList = getLemmalist(word.trim().toLowerCase(), luceneMorphology);
        if (checkMorphPart(lemmaList.get(0).getPart())) {
            iterateLemmaList(tempResult, lemmaList);
        }
    }

    private static void iterateLemmaList(Map<String, Integer> tempResult, List<LemmaHelper> lemmaList) {
        lemmaList.forEach(l -> {
            if (tempResult.get(l.getForm()) == null) {
                tempResult.put(l.getForm(), 1);
            } else {
                int count = tempResult.get(l.getForm());
                tempResult.replace(l.getForm(), count, count + 1);
            }
        });
    }

    private Boolean checkMorphPart(String inf) {
        return !((inf.equals("СОЮЗ")) || (inf.equals("МЕЖД")) || (inf.equals("ЧАСТ"))
                || (inf.equals("ПРЕДЛ")) || (inf.equals("МС")));
    }

    private List<LemmaHelper> getLemmalist(String word, LuceneMorphology luceneMorphology) {
        List<LemmaHelper> result = new ArrayList<>();
        List<String> listNormal = luceneMorphology.getNormalForms(word);
        listNormal.stream().map(n -> new LemmaHelper(word, n))
                .peek(lH -> lH.customization(luceneMorphology))
                .forEach(result::add);
        return result;
    }

    public String getRussianText(String body) {
        Pattern pattern = Pattern.compile(RUSSIAN_REG_EXP);
        Matcher matcher = pattern.matcher(body);
        StringBuilder russianText = new StringBuilder();
        while (matcher.find()) {
            russianText.append(matcher.group(0));
        }
        return russianText.toString();
    }
}
