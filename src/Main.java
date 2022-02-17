import utils.ListModifiers;
import utils.Utils;
import utils.WordData;
import utils.WordScoring;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class Main {
    static int threadCount = 12; //The number of threads

    static boolean actualGame = true;
//    static String startingGuess = "saline";
//    static String startingGuess = "tares";

    static String startingGuess = "tares";


    public static void main(String[] args) throws IOException, InterruptedException {
        File guesses = new File("src/words/guesses.txt");
        File answers = new File("src/words/answers.txt");

        BufferedReader guessesReader = new BufferedReader(new FileReader(guesses));
        BufferedReader answersReader = new BufferedReader(new FileReader(answers));

        String st;

        List<String> unsortedWords = new ArrayList<>();
        List<String> unsortedAnswers = new ArrayList<>();

        while((st = guessesReader.readLine()) != null) {
            unsortedWords.add(st);
        }

        while((st = answersReader.readLine()) != null) {
            unsortedWords.add(st);
            unsortedAnswers.add(st);
        }


        List<String> sortedList = sortWordList(unsortedWords, true);
        System.out.println(sortedList);

        if(actualGame) {
            //Normal game loop
            while (true) {
                mainGuessingLoop(unsortedWords, unsortedAnswers);
            }
        } else {
            //Simulation
            simulateGames(unsortedWords, unsortedAnswers);
        }
    }

    private static void mainGuessingLoop(List<String> wordList, List<String> unsortedAnswers) throws IOException, InterruptedException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in)); //Create a buffered reader to read the inputs
        boolean useOnlyAnswers;

        System.out.println("First guess, as always, is " + startingGuess);
        String info = inputReader.readLine(); //Get the info about the last guess

        useOnlyAnswers = Utils.switchToAnswers(info); //Evaluate the info to see if we should switch to using only answers

        //If the word guess is correct, end the loop
        if(info.toLowerCase(Locale.ROOT).equals("correct")) return;

        //Update the sorted list of words
        wordList = ListModifiers.updateList(startingGuess, info, wordList);

        List<String> sortedAnswers;

        int timesGuessed = 1;
        //We only get 6 guesses and we've already guessed once
        while(timesGuessed < 6) {

            //Re-evaluate the word list to find the word that will give the fewest answers on average
            wordList = sortWordList(wordList);

            //Update the sorted list of answers
            sortedAnswers = new ArrayList<>();
            for(String s : wordList) {
                if(unsortedAnswers.contains(s)) sortedAnswers.add(s);
            }

            String modeOutput = !useOnlyAnswers ? "all words" : "answers only";
            System.out.println("Current mode: " + modeOutput);

            System.out.println("Remaining valid words " + wordList); //Output the set of remaining valid words
            System.out.println("Size of valid word list: " + wordList.size());

            String guess = useOnlyAnswers ? sortedAnswers.get(0) :wordList.get(0); //Get the guess based on whether or not we are using only words from the answer set
            System.out.println("Guess #" + (timesGuessed+1) + ". " + guess);

            info = inputReader.readLine(); //Get the info about the last guess
            useOnlyAnswers = Utils.switchToAnswers(info); //Evaluate the info to see if we should switch to using only answers

            //If the word guess is correct, end the loop
            if(info.toLowerCase(Locale.ROOT).equals("correct") || info.toLowerCase(Locale.ROOT).equals("ggggg")) {
                break;
            }

            //Remove the available words based on the guess and the info
            wordList = ListModifiers.updateList(guess, info, wordList);

            timesGuessed++;
        }
    }

    private static void simulateGames(List<String> originalList, List<String> unsortedAnswers) throws InterruptedException {
        List<GameData> games = new ArrayList<>();
        List<GameData> failedGames = new ArrayList<>();
        System.out.println("Starting simulations");

        System.out.print("Percent complete: 00.0%");

        for(String w : unsortedAnswers) {
            List<String> wordList = List.copyOf(originalList);

            List<String> sortedAnswers;
            int guesses = 1;
            boolean success = false;
            boolean useOnlyAnswers = false;

            //Begin with one single starting guess
            String guess = startingGuess;
            String info = getInfoFromWord(guess, w);

            if(info.equals("ggggg")) success = true;

            //Update the sorted list of words
            wordList = ListModifiers.updateList(startingGuess, info, wordList);

            while(guesses <= 6 && !success) {
                guesses++;

                //Re-evaluate the word list to find the word that will give the fewest answers on average
                wordList = sortWordList(wordList);

                //Update the sorted list of answers
                sortedAnswers = new ArrayList<>();
                for(String s : wordList) {
                    if(unsortedAnswers.contains(s)) sortedAnswers.add(s);
                }

                //Generate and apply the new guess for this attempt
                if(sortedAnswers.size() > 0) guess = useOnlyAnswers ? sortedAnswers.get(0) :wordList.get(0); //Get the guess based on whether or not we are using only words from the answer set
                else  {
                    break;
                }

                info = getInfoFromWord(guess, w); //Get the info about the last guess
                useOnlyAnswers = Utils.switchToAnswers(info); //Evaluate the info to see if we should switch to using only answers

                if(info.toLowerCase(Locale.ROOT).equals("ggggg")) {
                    success = true;
                    break;
                }

                //Remove the available words based on the guess and the info
                wordList = ListModifiers.updateList(guess, info, wordList);

            }

            GameData thisGame = new GameData(success, guesses, w);

            if(!success) {
                failedGames.add(thisGame);
            }

            games.add(thisGame);

            //Output how far through we are with nice pretty formatting
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

        double totalGuesses = 0;
        double totalSuccesses = 0;

        for(GameData d : games) {
            totalGuesses += d.isSuccess() ? d.getGuesses() : 0;
            totalSuccesses += d.isSuccess() ? 1 : 0;
        }

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

    private static String getInfoFromWord(String guess, String answer) {
        List<Character> answerList = Utils.stringToCharList(answer);

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

        return Utils.charListToString(infoList);
    }

    private static boolean rightIndex(Character c, Integer index, String answer) {
        int lowerBound = 0;
        while(answer.indexOf(c, lowerBound) != -1) {
            if(answer.indexOf(c, lowerBound) == index) return true;
            lowerBound = answer.indexOf(c, lowerBound) + 1;
        }

        return false;
    }

    private static List<String> sortWordList(List<String> unsortedWords) throws InterruptedException {
        int wordsPerJump = unsortedWords.size() / threadCount;

        List<WordData> allWordData = new ArrayList<>();
        List<String> sortedList = new ArrayList<>();

        //Decide whether to multithread or not
        if(unsortedWords.size() > 50) {

            List<ScoringThread> threads = new ArrayList<>();

            for (int i = 0; i < threadCount; i++) {
                List<String> subList;
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
            while (!allFinished) {
                allFinished = true;
                for (ScoringThread t : threads) {
                    if (!t.isFinished()) allFinished = false;
                }

                //            System.out.println("Waiting for all threads to finish...");

                Thread.sleep(50);
            }

            for (ScoringThread t : threads) {
                allWordData.addAll(t.getSortedList());
            }

            Collections.sort(allWordData, Comparator.comparingDouble(WordData::getScore));
        } else {
            allWordData = WordScoring.sortWordList(unsortedWords, unsortedWords, 0);
        }

        for (WordData d : allWordData) {
            sortedList.add(d.getWord());
        }

        return sortedList;
    }

}
