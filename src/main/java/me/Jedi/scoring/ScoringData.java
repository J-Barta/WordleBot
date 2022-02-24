package me.Jedi.scoring;

import me.Jedi.util.WordData;

public class ScoringData {
    private WordData word;
    private double score;

    public ScoringData(WordData word, double score) {
        this.word = word;
        this.score = score;
    }

    public WordData getWord() {
        return word;
    }

    public double getScore() {
        return score;
    }
}
