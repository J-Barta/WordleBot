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

        System.out.println("List after testing trawl " + Utils.updateList("trawl", "ggyng", unsortedAnswers));
        Thread.sleep(5000);
//        List<String> sortedList = sortWordList(unsortedWords);
        List<String> sortedList = sortWordList(unsortedAnswers);

        List<String> sortedAnswers = new ArrayList<>();

        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

        boolean useOnlyAnswers = false;
        int timesGuessed = 1;
        while(timesGuessed <= 6) {
            String modeOutput = !useOnlyAnswers ? "all words" : "answers only";
            System.out.println("Current mode: " + modeOutput);

            System.out.println("Remaining valid words " + sortedList); //Output the set of remaining valid words

            String guess = useOnlyAnswers ? sortedAnswers.get(0) :sortedList.get(0); //Get the guess based on whether or not we are using only words from the answer set
            System.out.println("Guess #" + timesGuessed + ". " + guess);

            String info = inputReader.readLine(); //Get the info about the last guess
            useOnlyAnswers = Utils.switchToGuesses(info); //Evaluate the info to see if we should switch to using only answers

            //If the word guess is correct, end the loop
            if(info.toLowerCase(Locale.ROOT).equals("correct")) {
                break;
            }

            //Update the sorted list of words
            sortedList = Utils.updateList(guess, info, sortedList);

            //Re-evaluate the word list to find the word that will give the fewest answers on average
            sortedList = sortWordList(sortedList);

            //Update the sorted list of answers
            sortedAnswers = new ArrayList<>();
            for(String s : sortedList) {
                if(unsortedAnswers.contains(s)) sortedAnswers.add(s);
            }

            timesGuessed++;
        }
    }

    private static List<String> sortWordList(List<String> unsortedWords) throws InterruptedException {
        int wordsPerJump = unsortedWords.size() / threadCount;

        List<Multithread> threads = new ArrayList<>();

        for(int i = 0; i< threadCount; i++) {
            //If this is the last thread, go to the end of the list
            if(i + 1 == threadCount) {
                threads.add(new Multithread(unsortedWords.subList(i*wordsPerJump, unsortedWords.size())));
                System.out.println("Started thread " + i + " with range " + i*wordsPerJump + " to " + unsortedWords.size());
            } else {
                threads.add(new Multithread(unsortedWords.subList(i*wordsPerJump, (i+1)*wordsPerJump-1)));
                System.out.println("Started thread " + i + " with range " + i*wordsPerJump + " to " + ((i+1)*wordsPerJump-1));
            }
            threads.get(i).start();
        }

        boolean allFinished = false;
        while(!allFinished) {
            allFinished = true;
            for(Multithread t : threads) {
                if(!t.isFinished()) allFinished = false;
            }

//            System.out.println("Waiting for all threads to finish...");

            Thread.sleep(50);
        }

        List<WordData> allWordData = new ArrayList<>();

        for(Multithread t : threads) {
            allWordData.addAll(t.getSortedList());
        }

        Collections.sort(allWordData, Comparator.comparingDouble(WordData::getScore));

        for(WordData d : allWordData) {
            System.out.println("Word: " + d.getWord() + " - score: " + d.getScore());
        }

        List<String> sortedList = new ArrayList<>();
        for(WordData d : allWordData) {
            sortedList.add(d.getWord());
        }

        return sortedList;
    }

}
