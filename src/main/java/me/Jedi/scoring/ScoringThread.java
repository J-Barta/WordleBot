package me.Jedi.scoring;

import me.Jedi.drivers.LatinDriver;
import me.Jedi.util.WordData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScoringThread extends Thread{
    List<WordData> toSort;
    List<WordData> fullList;
    int id;

    List<ScoringData> wordsWithData = new ArrayList<>();

    private boolean finished = false;

    WordScoring scoring;


    public ScoringThread(List<WordData> toSort, List<WordData> fullList, int id) {
        super();
        this.toSort = toSort;
        this.fullList = fullList;
        this.id = id;
        scoring = new WordScoring();
    }

    @Override
    public void run() {
        try {
            wordsWithData = scoring.sortWordList(toSort, fullList);
            finished = true;
        }
        catch (Exception e) {
            System.out.println("Exception is caught");
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public double wordsSearched() {
        return scoring.totalSearched;
    }

    public List<ScoringData> getSortedList() {
        return wordsWithData;
    }
}
