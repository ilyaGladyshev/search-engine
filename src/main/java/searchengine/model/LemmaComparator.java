package searchengine.model;

import java.util.Comparator;

public class LemmaComparator implements Comparator<Lemma> {
    @Override
    public int compare(Lemma o1, Lemma o2) {
        return o1.getFrequency() - o2.getFrequency();
    }
}
