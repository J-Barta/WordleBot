package me.Jedi;

import me.Jedi.scoring.Sorting;
import me.Jedi.util.ListModifiers;
import me.Jedi.util.Utils;
import me.Jedi.util.WordData;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private List<WordData> wordList;
    private List<WordData> unsortedAnswers;
    private String firstGuess;
    private int guesses;
    private boolean useOnlyAnswers;
    private boolean forceNotAnswers;
    private String correctInfo;

    private String currentGuess = "";

    public Game(List<WordData> originalList, List<WordData> unsortedAnswers, String firstGuess)   {
        this.wordList = originalList;
        this.unsortedAnswers = unsortedAnswers;
        this.firstGuess = firstGuess;

        guesses = 0;
        useOnlyAnswers = false;
        forceNotAnswers = false;

        List<Character> firstWord = unsortedAnswers.get(0).getLetters();
        List<Character> infoList = new ArrayList<>();
        for(int i = 0; i < firstWord.size(); i++) {
            infoList.add('g');
        }

        correctInfo = Utils.charListToString(infoList);
    }

    public String getNextGuess(boolean showTelemetry, boolean forceSingleThread) throws InterruptedException {
        guesses ++;

        if(guesses == 1) currentGuess =  firstGuess;
        else {
            if (forceNotAnswers) useOnlyAnswers = false;

            wordList = Sorting.sortWordList(wordList, showTelemetry, forceSingleThread);

            if(wordList.size() == 0) return null;

            if (useOnlyAnswers)  {
                int i =0;
                while(!unsortedAnswers.contains(wordList.get(i))) i++;
                return currentGuess = wordList.get(i).getWord();
            }

            else currentGuess = wordList.get(0).getWord();
        }

        return currentGuess;
    }

    public String getNextGuess(boolean showTelemetry) throws InterruptedException {
        return getNextGuess(showTelemetry, false);
    }

    public String getNextGuess() throws InterruptedException {
        return getNextGuess(true);
    }

    /**
     *
     * @param info
     * @return isFinished
     */
    public boolean updateList(String info) {
        useOnlyAnswers = Utils.switchToAnswers(info);
        boolean correct = info.toLowerCase().equals(correctInfo);
        if(correct) return true;
        else {
            wordList = ListModifiers.updateList(currentGuess, info, wordList);

            return false;
        }
    }

    public List<WordData> getRemainingValidWords() {
        return wordList;
    }

    public boolean isUseOnlyAnswers() {
        return useOnlyAnswers;
    }

    public int getGuesses() {
        return guesses;
    }

    public void forceNotAnswers() {
        forceNotAnswers = true;
    }
}
