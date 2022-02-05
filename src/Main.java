import java.io.*;
import java.util.*;

public class Main {


    public static void main(String[] args) throws IOException {
        //TODO: Distinction between answers and acceptable words
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

        List<String> sortedList = sortWordList(unsortedWords);
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
            useOnlyAnswers = switchToGuesses(info); //Evaluate the info to see if we should switch to using only answers

            //If the word guess is correct, end the loop
            if(info.toLowerCase(Locale.ROOT).equals("correct")) {
                break;
            }

            //Update the sorted list of words
            sortedList = updateList(guess, info, sortedList);

            //Update the sorted list of answers
            sortedAnswers = new ArrayList<>();
            for(String s : sortedList) {
                if(unsortedAnswers.contains(s)) sortedAnswers.add(s);
            }

            timesGuessed++;
        }
    }

    private static boolean switchToGuesses(String info) {
        double correctPercentage = 0;
        for(int i = 0; i < info.length(); i++) {
            if(info.charAt(i) == 'y') correctPercentage += 10;
            if(info.charAt(i) == 'g') correctPercentage += 20;
        }

        return correctPercentage >= 40;
    }

    /**
     * 'n' = not in the word; 'y' = wrong location in the word; 'g' = correct letter in correct location
     * @param info 5 characters that describe the previous guess
     * @param words
     * @return
     */
    private static List<String> updateList(String guess, String info, List<String> words) {
        List<String> correctedWords = words;
        for(int i = 0; i<info.length(); i++) {
            Character c  = info.charAt(i);
            if(c.equals('n')) {
                correctedWords = removeLetter(guess.charAt(i), correctedWords);
            } else if(c.equals('y')) {
                correctedWords = removeWordsWithoutLetter(guess.charAt(i), correctedWords);
                correctedWords = removeWordsWithLetterAtIndex(guess.charAt(i), i, correctedWords);
            } else if(c.equals('g')) {
                correctedWords = removeWordsWithoutLetterAtIndex(guess.charAt(i), i, correctedWords);

            }
        }

        return correctedWords;
    }

    /**
     * This method should be used to remove letters that are definitely not in the word (i.e. gray letters)
     * @param letter the letter that should be removed from the word
     * @param words the list of words
     * @return the fixed list
     */
    private static List<String> removeLetter(Character letter, List<String> words) {
        List<String> correctedList = new ArrayList<>();
        for(String s : words) {
            if(!(s.indexOf(letter) != -1)) correctedList.add(s);
        }

        return correctedList;

    }

    /**
     * Used to remove all words from the list if they don't have a specific letter (i.e. if the letter is yellow)
     * @param letter the letters that MUST be in the returned words
     * @param words the current list of words
     * @return the only remaining words that have the specified letter
     */
    private static List<String> removeWordsWithoutLetter(Character letter, List<String> words) {
        List<String> correctedList = new ArrayList<>();

        for(String s : words) {
            if(s.indexOf(letter) != -1) correctedList.add(s);
        }

        return correctedList;
    }

    /**
     * Used to remove all words that have a letter at a specific index (used for yellow letters)
     * @param c
     * @param index
     * @param words
     * @return
     */
    private static List<String> removeWordsWithLetterAtIndex(Character c, Integer index, List<String> words) {

        //TOOD: Support duplicate letters in a word
        List<String> correctedList = new ArrayList<>();

        for(String s : words) {
            int lowerBound = 0;
            boolean shouldRemove = false;
            while(s.indexOf(c, lowerBound) != -1) {
                if(s.indexOf(c, lowerBound) == index) {
                    shouldRemove = true;
                }

                lowerBound = s.indexOf(c, lowerBound) + 1;
            }
            if(!shouldRemove) correctedList.add(s);
        }

        return correctedList;
    }

    /**
     *
     * @param c
     * @param index
     * @param words
     * @return
     */
    private static List<String> removeWordsWithoutLetterAtIndex(Character c, Integer index, List<String> words) {
        //TOOD: Support duplicate letters in a word
        List<String> correctedList = new ArrayList<>();

        for(String s : words) {
            int lowerBound = 0;
            while(s.indexOf(c, lowerBound) != -1) {
                if (s.indexOf(c, lowerBound) == index) {
                    correctedList.add(s);
                    break;
                } else {
                    lowerBound = s.indexOf(c, lowerBound) + 1;
                }
            }
        }

        return correctedList;
    }


    private static List<String> sortWordList(List<String> unsortedList) {
        List<WordData> wordsWithWeight = new ArrayList<>();
        //Evaluate the score of each word
        for(String s : unsortedList) {
            wordsWithWeight.add(new WordData(s, evaluateWord(s, ValueMode.SingleLetter)));
        }

        //Sort the words by weight
        Collections.sort(wordsWithWeight, Comparator.comparingDouble(WordData::getScore));
        Collections.reverse(wordsWithWeight);

        List<String> orderedWords = new ArrayList<>();
        for(WordData d : wordsWithWeight) {
//            System.out.println(d.getWord() + " - " + d.getScore());
            orderedWords.add(d.getWord());
        }
        return orderedWords;
    }

    private static double evaluateWord(String word, ValueMode mode) {
        double totalScore = 0;
        List<Character> usedLetters = new ArrayList<>();

        for(int i = 0; i<word.length(); i++) {
            boolean letterUsed = mode == ValueMode.SingleLetter && usedLetters.contains(word.charAt(i));
            totalScore += letterUsed ? 0 : Letters.letters.get((word.charAt(i)));
            usedLetters.add(word.charAt(i));
        }
        return totalScore;
    }

    private enum ValueMode {
        DoubleLetter, //We care about the double letters (should be used later in the guesses)
        SingleLetter //We only care about the letters in the word (not how many of each letter there is)
    }
}
