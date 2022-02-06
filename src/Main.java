import java.io.*;
import java.util.*;

public class Main {
    static int threadCount = 12; //The number of threads


    public static void main(String[] args) throws IOException, InterruptedException {
        File guesses = new File("src/guesses.txt");
        File answers = new File("src/answers.txt");

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

        while(true) {
            mainGuessingLoop(unsortedWords, unsortedAnswers);

        }
    }

    private static void mainGuessingLoop(List<String> sortedList, List<String> unsortedAnswers) throws IOException, InterruptedException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in)); //Create a buffered reader to read the inputs
        boolean useOnlyAnswers;

        System.out.println("First guess, as always, is tares");
        String info = inputReader.readLine(); //Get the info about the last guess

        useOnlyAnswers = Utils.switchToGuesses(info); //Evaluate the info to see if we should switch to using only answers

        //If the word guess is correct, end the loop
        if(info.toLowerCase(Locale.ROOT).equals("correct")) return;

        //Update the sorted list of words
        sortedList = Utils.updateList("tares", info, sortedList);


        List<String> sortedAnswers;

        int timesGuessed = 1;
        //We only get 6 guesses and we've already guessed once
        while(timesGuessed < 6) {

            //Re-evaluate the word list to find the word that will give the fewest answers on average
            sortedList = sortWordList(sortedList);

            //Update the sorted list of answers
            sortedAnswers = new ArrayList<>();
            for(String s : sortedList) {
                if(unsortedAnswers.contains(s)) sortedAnswers.add(s);
            }

            String modeOutput = !useOnlyAnswers ? "all words" : "answers only";
            System.out.println("Current mode: " + modeOutput);

            System.out.println("Remaining valid words " + sortedList); //Output the set of remaining valid words

            String guess = useOnlyAnswers ? sortedAnswers.get(0) :sortedList.get(0); //Get the guess based on whether or not we are using only words from the answer set
            System.out.println("Guess #" + (timesGuessed+1) + ". " + guess);

            info = inputReader.readLine(); //Get the info about the last guess
            useOnlyAnswers = Utils.switchToGuesses(info); //Evaluate the info to see if we should switch to using only answers

            //If the word guess is correct, end the loop
            if(info.toLowerCase(Locale.ROOT).equals("correct")) {
                break;
            }

            //Update the sorted list of words
            sortedList = Utils.updateList(guess, info, sortedList);

            timesGuessed++;
        }
    }

    private static List<String> sortWordList(List<String> unsortedWords) throws InterruptedException {
        int wordsPerJump = unsortedWords.size() / threadCount;

        List<WordData> allWordData = new ArrayList<>();
        List<String> sortedList = new ArrayList<>();

        //Decide whether to multithread or not
        if(unsortedWords.size() > 100) {

            List<Multithread> threads = new ArrayList<>();

            for (int i = 0; i < threadCount; i++) {
                //If this is the last thread, go to the end of the list
                if (i + 1 == threadCount) {
                    threads.add(new Multithread(unsortedWords.subList(i * wordsPerJump, unsortedWords.size()), unsortedWords, i));
                    System.out.println("Started thread " + i + " with range " + i * wordsPerJump + " to " + unsortedWords.size());
                } else {
                    threads.add(new Multithread(unsortedWords.subList(i * wordsPerJump, (i + 1) * wordsPerJump - 1), unsortedWords, i));
                    System.out.println("Started thread " + i + " with range " + i * wordsPerJump + " to " + ((i + 1) * wordsPerJump - 1));
                }
                threads.get(i).start();
            }

            boolean allFinished = false;
            while (!allFinished) {
                allFinished = true;
                for (Multithread t : threads) {
                    if (!t.isFinished()) allFinished = false;
                }

                //            System.out.println("Waiting for all threads to finish...");

                Thread.sleep(50);
            }

            for (Multithread t : threads) {
                allWordData.addAll(t.getSortedList());
            }

            Collections.sort(allWordData, Comparator.comparingDouble(WordData::getScore));

            for (WordData d : allWordData) {
                System.out.println("#" + allWordData.indexOf(d) + " Word: " + d.getWord() + " - score: " + d.getScore());
            }
        } else {
            allWordData = Utils.sortWordList(unsortedWords, unsortedWords, 0);
        }

        for (WordData d : allWordData) {
            sortedList.add(d.getWord());
        }

        return sortedList;
    }

}
