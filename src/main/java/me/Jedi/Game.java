package me.Jedi;

import me.Jedi.utils.ListModifiers;
import me.Jedi.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private List<String> wordList;
    private List<String> unsortedAnswers;
    private List<String> sortedAnswers;
    private String firstGuess;
    private int guesses;
    private boolean useOnlyAnswers;
    private boolean forceNotAnswers;
    private String correctInfo;

    private String currentGuess = "";

    public Game(List<String> originalList, List<String> unsortedAnswers, String firstGuess)   {
        this.wordList = originalList;
        this.unsortedAnswers = unsortedAnswers;
        this.firstGuess = firstGuess;

        this.sortedAnswers = new ArrayList<>();

        guesses = 0;
        useOnlyAnswers = false;
        forceNotAnswers = false;

        List<Character> firstWord = Utils.stringToCharList(unsortedAnswers.get(0));
        List<Character> infoList = new ArrayList<>();
        for(int i = 0; i < firstWord.size(); i++) {
            infoList.add('g');
        }

        correctInfo = Utils.charListToString(infoList);
    }

    public String getNextGuess() throws InterruptedException {
        guesses ++;

        if(guesses == 1) currentGuess =  firstGuess;
        else {
            if (forceNotAnswers) useOnlyAnswers = true;

            wordList = Main.sortWordList(wordList, true);

            sortedAnswers = new ArrayList<>();
            for (String s : wordList) {
                if (unsortedAnswers.contains(s)) sortedAnswers.add(s);
            }

            if (useOnlyAnswers) return currentGuess = sortedAnswers.get(0);
            else currentGuess = wordList.get(0);
        }

        return currentGuess;
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

    public List<String> getRemainingValidWords() {
        return wordList;
    }

    public boolean isUseOnlyAnswers() {
        return useOnlyAnswers;
    }

    public int getGuesses() {
        return guesses;
    }

    private void forceNotAnswers() {
        forceNotAnswers = true;
    }
}
