package me.Jedi.util;

import java.util.List;

public class WordData {
    private String word;
    private List<Character> letters;

    public WordData(String word) {
        this.word = word;

        letters = Utils.stringToCharList(word);
    }

    public String getWord() {
        return word;
    }

    public List<Character> getLetters() {
        return letters;
    }
}
