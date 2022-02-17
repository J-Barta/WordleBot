import utils.WordData;
import utils.WordScoring;

import java.util.ArrayList;
import java.util.List;

public class ScoringThread extends Thread{
     List<String> toSort;
    List<String> fullList;
    int id;

    List<WordData> wordsWithData = new ArrayList<>();

    private boolean finished = false;

    WordScoring scoring;

    public ScoringThread(List<String> toSort, List<String> fullList, int id) {
        super();
        this.toSort = toSort;
        this.fullList = fullList;
        this.id = id;
        scoring = new WordScoring();
    }

    @Override
    public void run() {
        try {
            wordsWithData = scoring.sortWordList(toSort, fullList, id);
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

    public List<WordData> getSortedList() {
        return wordsWithData;
    }
}
