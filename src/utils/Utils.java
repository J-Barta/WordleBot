package utils;

import java.text.DecimalFormat;
import java.util.*;


public class Utils {

    public static GuessMode mode = GuessMode.Normal;

    /**
     * @param info the clue given by the last guess
     * @return true if we should switch to using only answer words instead of evaluating all words
     */
    public static boolean switchToAnswers(String info) {
        double correctPercentage = 0;
        for(int i = 0; i < info.length(); i++) {
            if(info.charAt(i) == 'y') correctPercentage += 10;
            if(info.charAt(i) == 'g') correctPercentage += 20;
        }

        return correctPercentage >= 40;
    }


    public static String charListToString(List<Character> chars) {
        String toReturn = "";

        for(Character c : chars) {
            toReturn = toReturn + c;
        }

        return toReturn;
    }

    public static String charListToString(Character... chars) {
        return charListToString(List.of(chars));
    }

    public static List<Character> stringToCharList(String string) {
        List<Character> list = new ArrayList<>();

        for(int i = 0; i < string.length(); i++) {
            list.add(string.charAt(i));
        }

        return list;
    }

    public static boolean rightIndex(Character c, Integer index, String answer) {
        int lowerBound = 0;
        while(answer.indexOf(c, lowerBound) != -1) {
            if(answer.indexOf(c, lowerBound) == index) return true;
            lowerBound = answer.indexOf(c, lowerBound) + 1;
        }

        return false;
    }

    public static String getInfoFromWord(String guess, String answer) {
        List<Character> answerList = stringToCharList(answer);

        List<Character> infoList = new ArrayList<>();

        for(int i = 0; i<guess.length(); i++) {
            Character c = guess.charAt(i);
            if(!answerList.contains(c)) infoList.add('n');
            else {
                if(rightIndex(c,i, answer)) {
                    infoList.add('g');
                } else {
                    infoList.add('y');
                }
            }
        }

        return charListToString(infoList);
    }

    public static void printSimulationProgress(List<String> unsortedAnswers, String w) {
        System.out.print("\b\b\b\b\b");
        double fractionComplete = ((double) unsortedAnswers.indexOf(w) + 1) / unsortedAnswers.size();
        DecimalFormat df = new DecimalFormat("##.#");
        String percentage = df.format(fractionComplete * 100) + "%";
        if(percentage.indexOf('.') == -1) percentage = percentage.substring(0, percentage.indexOf('%')-1) + ".0%";
        while(percentage.length() < 5) {
            percentage = "0" + percentage;
        }
        System.out.print(percentage);
    }

    public static void printSimulationStats(List<GameData> games, List<GameData> failedGames, double totalGuesses, double totalSuccesses) {
        for(GameData d : failedGames) {
            System.out.println("failed game: " + d.getAnswer());
        }

        System.out.println("Total games: " + games.size());
        System.out.println("Total Successes: " + totalSuccesses);
        System.out.println("Total Failures: " + (games.size() - totalSuccesses));
        System.out.println("Success Rate: " + (totalSuccesses / games.size()));
        System.out.println("Average guesses: " + (totalGuesses / totalSuccesses));
        GameData bestGame = games.stream().min(Comparator.comparingDouble(GameData::getGuesses)).get();
        System.out.println("Best guess: " + bestGame.getAnswer() + " with " + bestGame.getGuesses() + " guesses");
    }

    public static String getGuess(List<String> currentWordList, List<String> sortedAnswers, List<String> originalList, String lastGuess, String lastInfo, boolean useOnlyAnswers, int guessNumber) {
        String guess;
        if(currentWordList.size() == 0) return null;

        if(useOnlyAnswers) {
            if(useAlternateEndgame(sortedAnswers, 6-guessNumber)) {
                mode = GuessMode.Special;
                System.out.println("Doing alternate endgame algorithm guess");
                guess = getAlternateEndgameGuess(lastGuess, lastInfo, originalList, currentWordList);
            } else {
                mode = GuessMode.Answers;
                guess = sortedAnswers.get(0);
            }
        }
        else  {
            mode = GuessMode.Normal;
            guess = currentWordList.get(0); //Guess from the normal word list
        }

        return guess;
    }

    private static boolean useAlternateEndgame(List<String> sortedAnswers, int remainingGuesses) {
        //Return
        if(sortedAnswers.size() == 1) return false;
        if(remainingGuesses == 1) return false; //We just have to make our last guess at this point
        if(sortedAnswers.size() > remainingGuesses && sortedAnswers.size() < 30) {
            return true;
        }
        else return false;
    }

    private static String getAlternateEndgameGuess(String previousGuess, String previousInfo, List<String> originalList, List<String> remainingWords) {
        List<Character> uniqueLetters = getUniqueUnsureLetters(remainingWords, previousGuess, previousInfo);

        List<WordData> wordScores = new ArrayList<>();

        for(String w : originalList) {
            List<Character> wordList = stringToCharList(w);
            double totalScore = 0;

            for(Character l : uniqueLetters) {
                totalScore += wordList.contains(l) ? 1 : 0;
            }

            wordScores.add(new WordData(w, totalScore));
        }

        Collections.sort(wordScores, Comparator.comparingDouble(WordData::getScore));
        Collections.reverse(wordScores);

        wordScores = adjustScores(wordScores);

        System.out.println("Found best word to be " + wordScores.get(0).getWord() + " with score " + wordScores.get(0).getScore());
        return wordScores.get(0).getWord();
    }

    /**
     * Adjust the scores of a word based on probabilities of letters
     * @return
     */
    private static List<WordData> adjustScores(List<WordData> currentData) {
        List<WordData> adjustedData = new ArrayList<>();
        for(WordData d : currentData) {
            double score = d.getScore() * getCompoundProbability(d.getWord());

            adjustedData.add(new WordData(d.getWord(), score));
        }
        return adjustedData;
    }

    private static double getCompoundProbability(String word) {
        List<Character> wordList = stringToCharList(word);
        double probability = 0;
        List<Character> usedChars = new ArrayList<>(); //Stop duplicate letters from being counted
        for(Character c : wordList) {
            probability += !usedChars.contains(c) ? Letters.letters.get(c) : 0;
            usedChars.add(c);
        }

        return probability;
    }

    private static List<Character> getUniqueUnsureLetters(List<String> remainingWords, String guess, String info) {
        List<Character> uniqueLetters = new ArrayList<>();

        List<Character> sureLetters = new ArrayList<>();
        List<Character> guessList = stringToCharList(guess);
        List<Character> infoList = stringToCharList(info);

        for(int i = 0; i< guessList.size(); i++ ) {
            if(infoList.get(i) == 'g') sureLetters.add(guessList.get(i));
        }


        for(String s : remainingWords) {
            List<Character> charList = stringToCharList(s);

            for(Character c : charList) {
                if(!uniqueLetters.contains(c) && !sureLetters.contains(c)) uniqueLetters.add(c);
            }
        }

        return uniqueLetters;
    }

    public static enum GuessMode {
        Normal, Answers, Special
    }

}