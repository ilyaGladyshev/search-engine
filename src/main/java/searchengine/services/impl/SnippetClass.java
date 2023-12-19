package searchengine.services.impl;

import lombok.Getter;

@Getter
public class SnippetClass {
    private String text;
    private int index;

    public SnippetClass(String text, int index) {
        this.text = text;
        this.index = index;
    }
}
