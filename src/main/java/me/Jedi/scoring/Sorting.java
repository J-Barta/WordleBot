package me.Jedi.scoring;

import me.Jedi.Main;
import me.Jedi.util.WordData;

import java.util.*;

import static me.Jedi.Main.threadCount;

public class Sorting {
    public static List<WordData> sortWordList(List<WordData> unsortedWords, boolean showTelemetry, boolean forceSingleThread) throws InterruptedException {
        int wordsPerJump = unsortedWords.size() / threadCount;

        List<ScoringData> allWordData = new ArrayList<>();
        List<WordData> sortedList = new ArrayList<>();

        //Decide whether to multi-thread or not
        long startTime = System.currentTimeMillis();
        if(unsortedWords.size() > 50 && !forceSingleThread) {

            List<ScoringThread> threads = new ArrayList<>();

            for (int i = 0; i < threadCount; i++) {
                List<WordData> subList;
                //If this is the last thread, go to the end of the list
                if (i + 1 == threadCount) {
                    subList = unsortedWords.subList(i * wordsPerJump, unsortedWords.size());
                } else {
                    subList = unsortedWords.subList(i * wordsPerJump, (i + 1) * wordsPerJump);
                }

                threads.add(new ScoringThread(subList, unsortedWords, i));

                threads.get(i).start();
            }

            boolean allFinished = false;
            if(showTelemetry) System.out.println();
            while (!allFinished) {
                allFinished = true;
                int totalScored = 0;
                for (ScoringThread t : threads) {
                    if (!t.isFinished()) allFinished = false;

                    totalScored += t.wordsSearched();
                }

                if(showTelemetry) System.out.print("\rProgress: " + totalScored + "/" + unsortedWords.size() + " words searched.");

                Thread.sleep(50);
            }

            for (ScoringThread t : threads) {
                allWordData.addAll(t.getSortedList());
            }

            Collections.sort(allWordData, Comparator.comparingDouble(ScoringData::getScore));
        } else {
            WordScoring scoring = new WordScoring();
            allWordData = scoring.sortWordList(unsortedWords, unsortedWords);
        }

        for (ScoringData d : allWordData) {
            sortedList.add(d.getWord());
        }

        if(Main.wordleType == Main.WordleTypes.Absurdle) Collections.reverse(sortedList);

        long endTime = System.currentTimeMillis();
        long runTime = endTime - startTime;

        if(showTelemetry) System.out.println();
        if(showTelemetry) System.out.println("Completed scoring " + sortedList.size() + " words. It took " + (runTime/1000.0) + " seconds. Speed: " + ((double) sortedList.size()) / (runTime/1000.0) + " words per second");
        if(showTelemetry) System.out.println("Best word: " + sortedList.get(0).getWord());

        return sortedList;
    }
}
