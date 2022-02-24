package me.Jedi.scoring;

import me.Jedi.util.Letters;
import me.Jedi.util.ListModifiers;
import me.Jedi.util.Utils;
import me.Jedi.util.WordData;

import java.util.*;

public class WordScoring {
    public double totalSearched = 0;

    public WordScoring() {}

    public List<ScoringData> sortWordList(List<WordData> unsortedList, List<WordData> fullList, int id) {
        List<ScoringData> wordsWithWeight = new ArrayList<>();

        //Evaluate the score of each word
        for(WordData s : unsortedList) {
            wordsWithWeight.add(new ScoringData(s, evaluateWord(s.getWord(), fullList)));
            totalSearched++;
        }

        //Sort the words by weight
        Collections.sort(wordsWithWeight, Comparator.comparingDouble(ScoringData::getScore));
//        Collections.reverse(wordsWithWeight);
        return wordsWithWeight;
    }

    /**
     * Function that evaluates the value of a word by iterating through every outcome from the guess and returning it's expected value
     * @param word
     * @param unsortedList
     * @return expected value
     */
    public static double evaluateWord(String word, List<WordData> unsortedList) {
        List<Character> charList = Utils.stringToCharList(word);
        //Generate a list equal to the length of the word with all "n"
        List<Character> info = getInitialList(charList);

        double totalValue = 0;
        int countedIterations = 0;
        while (true) {
            String correctedInfo = handleImpossibilities(word, Utils.charListToString(info));
            double thisValue = ListModifiers.updateList(word, correctedInfo, unsortedList).size();

            totalValue += thisValue;
            if(thisValue > 0) countedIterations++;

            if(recursiveUpdateList(info))  {
                break;
            }

        }

//        System.out.println("Scored word " + word + " as " + totalValue / countedIterations + " (with " + countedIterations + " counted iterations)");
        return totalValue / (double) countedIterations;
    }

    public static List<Character> getInitialList(List<Character> charList) {
        List<Character> info = new ArrayList<>();

        for(int i = 0; i<charList.size(); i++) {
            info.add('n');
        }
        return info;
    }

    private static boolean recursiveUpdateList(List<Character> info, int startingIndex) {
        boolean lastModified = false;
        boolean everyCharacterGreen = false;
        if(info.size() == startingIndex-1) {
            everyCharacterGreen = true;
            for(Character c : info) {
                if(c != 'g') everyCharacterGreen = false;
            }
        }

        if(!(info.size() == startingIndex + 1) && !everyCharacterGreen) {
            info.set(startingIndex, nextInfoChar(info.get(startingIndex)));
            if(info.get(startingIndex) == 'n' && info.size() != startingIndex -1) {
                lastModified = recursiveUpdateList(info, startingIndex+1);
            }
        } else {
            return true;
        }

        return lastModified;
    }

    private static boolean recursiveUpdateList(List<Character> info) {
       return recursiveUpdateList(info, 0);
    }

    /**
     * Corrects impossibilities in a word when evaluating it's score (i.e. a word that MUST have one letter and MUST also not have it)
     * @param word the original word
     * @param info the original info for the word
     * @return A modified version of the info that will be safe to use
     */
    private static String handleImpossibilities(String word, String info) {
        List<Character> infoList = Utils.stringToCharList(info);

        for(Character c : Letters.letters.keySet()) {
            int lowerBound = 0;
            List<Integer> countedIndices = new ArrayList<>();
            while(word.indexOf(c, lowerBound) != -1) {
                countedIndices.add(word.indexOf(c, lowerBound));
                lowerBound = word.indexOf(c, lowerBound) + 1;
            }

            //If the letter is counted more than once, check for impossibilities
            if(countedIndices.size() > 1) {
                //Impossible combinations: ny, ng
                //Acceptable combinations: gg, yy, yg, nn

                //The list of info that can potentially have
                List<Character> potentialErrors = new ArrayList<>();
                for(Integer i : countedIndices) {
                    potentialErrors.add(infoList.get(i));
                }

                for(Character infoChar : potentialErrors) {
                    //If an n exists here, then all instances of this letter need to be set to n
                    if(infoChar == 'n') {
                        //Set every index of potential errors to n in the infoList
                        for (Integer i : countedIndices) {
                            infoList.set(i, 'n');
                        }

//                        System.out.println("Located impossibility in word " + word + " with info " + info + ". Corrected info to " + charListToString(infoList));
                        break;
                    }
                }
            }
        }

        return Utils.charListToString(infoList);
    }

    public static Character nextInfoChar(Character c) {
        if(c == 'n') return 'y';
        else if(c == 'y') return 'g';
        else if(c == 'g') return 'n';

        return null;
    }
}
