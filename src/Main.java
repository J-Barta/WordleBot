import utils.*;

import java.io.*;
import java.util.*;

public class Main {
    static int threadCount = 12; //The number of threads

    static boolean actualGame = true;
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

    private static void mainGuessingLoop(List<String> originalList, List<String> unsortedAnswers) throws IOException, InterruptedException {
        List<String> wordList = List.copyOf(originalList);
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in)); //Create a buffered reader to read the inputs

        String guess = startingGuess;
        String lastNormalGuess = startingGuess; //Store the last normal guess (not the last special guess) for more intelligent guessing behavior

        System.out.println("First guess, as always, is " + guess);
        String info = inputReader.readLine(); //Get the info about the last guess
        String lastNormalInfo = info;


        boolean useOnlyAnswers = Utils.switchToAnswers(info); //Evaluate the info to see if we should switch to using only answers

        //If the word guess is correct, end the loop
        if(info.toLowerCase(Locale.ROOT).equals("correct")) return;

        //Update the sorted list of words
        wordList = ListModifiers.updateList(guess, info, wordList);

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

            guess = Utils.getGuess(wordList, sortedAnswers, originalList, lastNormalGuess, lastNormalInfo, useOnlyAnswers, timesGuessed); //Get the guess based on whether or not we are using only words from the answer set
            System.out.println("Guess #" + (timesGuessed+1) + ". " + guess);

            info = inputReader.readLine(); //Get the info about the last guess
            if(wordList.contains(guess)) useOnlyAnswers = Utils.switchToAnswers(info); //Evaluate the info to see if we should switch to using only answers

            //If the word guess is correct, end the loop
            if(info.toLowerCase(Locale.ROOT).equals("correct") || info.toLowerCase(Locale.ROOT).equals("ggggg")) {
                break;
            }

            //Remove the available words based on the guess and the info
            wordList = ListModifiers.updateList(guess, info, wordList);

            timesGuessed++;

            if(Utils.mode == Utils.GuessMode.Normal || Utils.mode == Utils.GuessMode.Answers) {
                lastNormalGuess = guess;
                lastNormalInfo = info;
            }
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
            String info = Utils.getInfoFromWord(guess, w);

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
                guess = Utils.getGuess(wordList, sortedAnswers, originalList, guess, info, useOnlyAnswers, guesses);
                if (guess == null) break;

                info = Utils.getInfoFromWord(guess, w); //Get the info about the last guess
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
            Utils.printSimulationProgress(unsortedAnswers, w);
        }
        
        System.out.println(""); //Make a new line so the next text isn't printed on top of the completion percent

        double totalGuesses = 0;
        double totalSuccesses = 0;

        for(GameData d : games) {
            totalGuesses += d.isSuccess() ? d.getGuesses() : 0;
            totalSuccesses += d.isSuccess() ? 1 : 0;
        }

        Utils.printSimulationStats(games, failedGames, totalGuesses, totalSuccesses);
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
