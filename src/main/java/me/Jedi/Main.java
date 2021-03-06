package me.Jedi;

import me.Jedi.drivers.Driver;
import me.Jedi.drivers.LatinDriver;
import me.Jedi.drivers.NormalDriver;
import me.Jedi.drivers.WordleGamesDriver;
import me.Jedi.scoring.*;
import me.Jedi.util.*;

import java.io.*;

import java.util.*;


public class Main {
    public static int threadCount = 100; //The number of threads

    public static final Mode gameMode = Mode.Manual;
    public static final WordleTypes wordleType = WordleTypes.Wordle;
    static final boolean doInitialSort = false;
    public static final int numberOfLetters = 5;
    static String startingGuess;
    static GameMode activeType;
    public static Map<me.Jedi.scoring.WordProperties.LetterProperty, List<WordData>> wordProperties;

    //TODO: Quordle
    //TODO: Wordle unlimited
    //TODO: Pre-compute word properties
    //TODO: make wordProperties static and public so it can be accessed. I think that would work better idk tho

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println((-1 % 6));
        
        GameMode wordleGames = new GameMode("all-words\\empty-list", "all-words\\words-" + numberOfLetters, "aasldfjas", new WordleGamesDriver(numberOfLetters));

        Map<WordleTypes, GameMode> gameModes = Map.ofEntries(
            Map.entry(WordleTypes.Wordle, new GameMode("guesses", "answers", "tares", new NormalDriver("https://www.nytimes.com/games/wordle/index.html"))),
            Map.entry(WordleTypes.Wordle6, new GameMode("guesses2", "answers2", "saline", new NormalDriver("https://www.wordle2.in"))),
            Map.entry(WordleTypes.Absurdle, new GameMode("guesses", "answers", "cocco", new NormalDriver(""))),
            Map.entry(WordleTypes.WordleLatin, new GameMode("guesses-latin", "answers-latin", "senia", new LatinDriver("https://wordle.latindictionary.io"))),
            Map.entry(WordleTypes.WordleGames, wordleGames)
        );

        activeType = gameModes.get(wordleType);


        List<WordData> unsortedWords = activeType.getWords();
        List<WordData> unsortedAnswers = activeType.getAnswers();
        startingGuess = activeType.getInitialGuess();

        if(doInitialSort) {
            List<WordData> sortedList = sortWordList(unsortedWords, true);
//            System.out.println("Complete sorted list: " + sortedList);
            System.out.println("Identified best word as " + sortedList.get(0));
            startingGuess = sortedList.get(0).getWord();
        }

        wordProperties = WordProperties.getWordProperties(unsortedWords);

        if(gameMode == Mode.Manual) {
            //Normal game loop
            while (true) {
                mainGuessingLoop(unsortedWords, unsortedAnswers);
            }
        }else if(gameMode == Mode.Automatic) {
          autoWin(unsortedWords, unsortedAnswers);
        } else {
            //Simulation
            simulateGames(unsortedWords, unsortedAnswers);
        }
    }

    private static void mainGuessingLoop(List<WordData> wordList, List<WordData> unsortedAnswers) throws IOException, InterruptedException {
        Game game = new Game(wordList, unsortedAnswers, startingGuess);

        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in)); //Create a buffered reader to read the inputs

        if(wordleType == WordleTypes.Absurdle) game.forceNotAnswers();


        //We only get 6 guesses, and we've already guessed once
        while(game.getGuesses() < 6 || wordleType == WordleTypes.Absurdle) {

            String modeOutput = !game.isUseOnlyAnswers() ? "all words" : "answers only";
            System.out.println("Current mode: " + modeOutput);

            System.out.println("Remaining valid words " + game.getRemainingValidWords()); //Output the set of remaining valid words
            System.out.println("Size of valid word list: " + game.getRemainingValidWords().size());

            String guess = game.getNextGuess();

            System.out.println("Guess #" + game.getGuesses() + ". " + guess);

            String info = inputReader.readLine(); //Get the info about the last guess
            if(game.updateList(info)) return;
        }
    }


    private static void autoWin(List<WordData> wordList, List<WordData> unsortedAnswers) throws InterruptedException {

        Game game = new Game(wordList, unsortedAnswers, startingGuess);

        Driver driver = activeType.getDriver();

        driver.open();

        if(wordleType == WordleTypes.Absurdle) game.forceNotAnswers();

        //We only get 6 guesses, and we've already guessed once
        while(game.getGuesses() < 6 || wordleType == WordleTypes.Absurdle) {

            String guess = game.getNextGuess(); //Get the guess based on whether we are using only words from the answer set
            Thread.sleep(1000);
            driver.typeWord(guess);

            if(game.updateList(driver.getInfo(game.getGuesses()))) break;
        }
    }

    private static void simulateGames(List<WordData> originalList, List<WordData> unsortedAnswers) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        List<GameData> games = new ArrayList<>();
        List<GameData> failedGames = new ArrayList<>();

        System.out.println("Starting simulations");

        List<SimulateThread> threads = new ArrayList<>();
        int wordsPerJump = unsortedAnswers.size() / threadCount;

        for (int i = 0; i < threadCount; i++) {
            List<WordData> subList;
            //If this is the last thread, go to the end of the list
            if (i + 1 == threadCount) {
                subList = unsortedAnswers.subList(i * wordsPerJump, unsortedAnswers.size());
            } else {
                subList = unsortedAnswers.subList(i * wordsPerJump, (i + 1) * wordsPerJump);
            }

            threads.add(new SimulateThread(subList, originalList, unsortedAnswers, startingGuess, wordProperties));

            threads.get(i).start();
        }

        boolean allFinished = false;

        while (!allFinished) {
            allFinished = true;
            int totalScored = 0;
            for (SimulateThread t : threads) {
                if (!t.isFinished()) allFinished = false;

                totalScored += t.getGamesPlayed();
            }

            double percentage = truncatePercentage(((double) totalScored / originalList.size()) * 100, 2);

            System.out.print("\rPercent complete: " + percentage + "%");

            Thread.sleep(50);
        }

        for (SimulateThread t : threads) {
            games.addAll(t.getGameData());
            failedGames.addAll(t.getFailedGames());
        }

        System.out.println("");

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

        long runTime = System.currentTimeMillis() - startTime;

        System.out.println("Took a total of " + (runTime / 1000.0) + " seconds with an average of " + ((double) games.size() / (runTime / 1000.0)) + " games per second.");
    }

    private static double truncatePercentage(double fractionComplete, double decimalPlaces) {
        double value = fractionComplete;

        value = value * Math.pow(10, decimalPlaces);
        value = Math.floor(value);
        value = value / Math.pow(10, decimalPlaces);

        return value;
    }

    public static List<WordData> sortWordList(List<WordData> unsortedWords, boolean showTelemetry) throws InterruptedException { return Sorting.sortWordList(unsortedWords, showTelemetry, false);}

    public enum WordleTypes {
        Wordle, Wordle6, Absurdle, WordleLatin, WordleGames
    }

    public enum Mode {
        Automatic, Manual, Simulate
    }

}
