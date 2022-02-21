package me.Jedi;

import me.Jedi.utils.*;

import java.io.*;

import java.util.*;


public class Main {
    static int threadCount = 12; //The number of threads

    static final Mode gameMode = Mode.Automatic;
    static final WordleTypes wordleType = WordleTypes.Wordle6;
    static final boolean doInitialSort = false;
    static String startingGuess;
    static GameMode activeType;

    //TODO: Multithreaded simulations
    //TODO: Quordle
    //TODO: HTML abuse or whatever

    public static void main(String[] args) throws IOException, InterruptedException {

        Map<WordleTypes, GameMode> gameModes = Map.ofEntries(
            Map.entry(WordleTypes.Wordle, new GameMode("guesses", "answers", "tares", "https://www.nytimes.com/games/wordle/index.html")),
            Map.entry(WordleTypes.Wordle6, new GameMode("guesses2", "answers2", "saline", "https://www.wordle2.in")),
            Map.entry(WordleTypes.Absurdle, new GameMode("guesses", "answers", "cocco", ""))
        );

        activeType = gameModes.get(wordleType);

        List<String> unsortedWords = activeType.getWords();
        List<String> unsortedAnswers = activeType.getAnswers();
        startingGuess = activeType.getInitialGuess();

        if(doInitialSort) {
            List<String> sortedList = sortWordList(unsortedWords, true);
            System.out.println("Complete sorted list: " + sortedList);
            System.out.println("Identified best word as " + sortedList.get(0));
            startingGuess = sortedList.get(0);
        }


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

        List<Character> firstWord = Utils.stringToCharList(unsortedAnswers.get(0));
        List<Character> infoList = new ArrayList<>();
        for(int i = 0; i < firstWord.size(); i++) {
            infoList.add('g');
        }

        int timesGuessed = 1;
        //We only get 6 guesses and we've already guessed once
        while(timesGuessed <= 6 || wordleType == WordleTypes.Absurdle) {

            //Re-evaluate the word list to find the word that will give the fewest answers on average
            wordList = sortWordList(wordList, true);

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


    private static void autoWin(List<String> wordList, List<String> unsortedAnswers) throws IOException, InterruptedException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in)); //Create a buffered reader to read the inputs
        boolean useOnlyAnswers;

        HTMLDriver driver = new HTMLDriver(activeType.getUrl());

        System.out.println("First guess, as always, is " + startingGuess);

        driver.typeWord(startingGuess);

        String info = driver.getInfo(1);

        useOnlyAnswers = Utils.switchToAnswers(info); //Evaluate the info to see if we should switch to using only answers

        //If the word guess is correct, end the loop
        if(info.toLowerCase(Locale.ROOT).equals("correct")) return;

        //Update the sorted list of words
        wordList = ListModifiers.updateList(startingGuess, info, wordList);

        List<String> sortedAnswers;

        List<Character> firstWord = Utils.stringToCharList(unsortedAnswers.get(0));
        List<Character> infoList = new ArrayList<>();
        for(int i = 0; i < firstWord.size(); i++) {
            infoList.add('g');
        }

        int timesGuessed = 1;
        //We only get 6 guesses and we've already guessed once
        while(timesGuessed <= 6 || wordleType == WordleTypes.Absurdle) {

            //Re-evaluate the word list to find the word that will give the fewest answers on average
            wordList = sortWordList(wordList, true);

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
            Thread.sleep(1500);
            driver.typeWord(guess);

            info = driver.getInfo(timesGuessed+1);
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
        long startTime = System.currentTimeMillis();


        List<Character> firstWord = Utils.stringToCharList(originalList.get(0));
        List<Character> infoList = new ArrayList<>();
        for(int i = 0; i < firstWord.size(); i++) {
            infoList.add('g');
        }

        String correctInfo = Utils.charListToString(infoList);

        for(String w : unsortedAnswers) {
            List<String> wordList = List.copyOf(originalList);

            List<String> sortedAnswers;
            int guesses = 1;
            boolean success = false;
            boolean useOnlyAnswers = false;

            //Begin with one single starting guess
            String guess = startingGuess;
            String info = getInfoFromWord(guess, w);

            if(info.equals(correctInfo)) success = true;

            //Update the sorted list of words
            wordList = ListModifiers.updateList(startingGuess, info, wordList);

            while(guesses <= 6 && !success) {
                guesses++;

                //Re-evaluate the word list to find the word that will give the fewest answers on average
                wordList = sortWordList(wordList, false);

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

                if(info.toLowerCase(Locale.ROOT).equals(correctInfo)) {
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
            System.out.print("\r");

            double fractionComplete = ((double) unsortedAnswers.indexOf(w) + 1) / unsortedAnswers.size();

            double percentage = truncatePercentage(fractionComplete * 100, 2);

            System.out.print("Percent complete: " + percentage + "%");
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

    private static double truncatePercentage(double fracitonComplete, double decimalPlaces) {
        double value = fracitonComplete;

        value = value * Math.pow(10, decimalPlaces);
        value = Math.floor(value);
        value = value / Math.pow(10, decimalPlaces);

        return value;
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

    public static List<String> sortWordList(List<String> usnortedWords, boolean showTelemetry) throws InterruptedException { return sortWordList(usnortedWords, showTelemetry, false);}

    private static List<String> sortWordList(List<String> unsortedWords, boolean showTelemetry, boolean forceSingleThread) throws InterruptedException {
        int wordsPerJump = unsortedWords.size() / threadCount;

        List<WordData> allWordData = new ArrayList<>();
        List<String> sortedList = new ArrayList<>();

        //Decide whether to multithread or not
        long startTime = System.currentTimeMillis();
        if(unsortedWords.size() > 50 && !forceSingleThread) {

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

            Collections.sort(allWordData, Comparator.comparingDouble(WordData::getScore));
        } else {
            WordScoring scoring = new WordScoring();
            allWordData = scoring.sortWordList(unsortedWords, unsortedWords, 0);
        }

        for (WordData d : allWordData) {
            sortedList.add(d.getWord());
        }

        if(wordleType == WordleTypes.Absurdle) Collections.reverse(sortedList);

        long endTime = System.currentTimeMillis();
        long runTime = endTime - startTime;

        if(showTelemetry) System.out.println();
        if(showTelemetry) System.out.println("Completed scoring " + sortedList.size() + " words. It took " + (runTime/1000.0) + " seconds. Speed: " + ((double) sortedList.size()) / (runTime/1000.0) + " words per second");

        return sortedList;
    }

    public enum WordleTypes {
        Wordle, Wordle6, Absurdle
    }

    public enum Mode {
        Automatic, Manual, Simulate
    }

}
