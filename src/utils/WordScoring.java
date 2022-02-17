package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WordScoring {
    public static List<WordData> sortWordList(List<String> unsortedList, List<String> fullList, int id) {
        List<WordData> wordsWithWeight = new ArrayList<>();
        //Evaluate the score of each word
//        System.out.print("Starting thread...");

        for(String s : unsortedList) {
            wordsWithWeight.add(new WordData(s, evaluateWord(s, fullList)));

//            System.out.print('\r');
//            System.out.print("Thread #" + id + " is " + ((unsortedList.indexOf(s) / (double) unsortedList.size()) * 100) + "% of the way done.");
        }

        //Sort the words by weight
        Collections.sort(wordsWithWeight, Comparator.comparingDouble(WordData::getScore));
        Collections.reverse(wordsWithWeight);
        return wordsWithWeight;
    }

    /**
     * Function that evaluates the value of a word by iterating through every outcome from the guess and returning it's expected value
     * @param word
     * @param unsortedList
     * @return expected value
     */
    public static double evaluateWord(String word, List<String> unsortedList) {

        Character[] info = {'n', 'n', 'n', 'n', 'n'};

        double totalValue = 0;
        int countedIterations = 0;
        while (true) {
            String correctedInfo = handleImpossibilities(word, Utils.charListToString(info));
            double thisValue = ListModifiers.updateList(word, correctedInfo, unsortedList, false).size();
//            if(charListToString(info).equals("nnnnn")) System.out.println("Scored word " + word + " with info " + correctedInfo + " as " + thisValue + " with word list size " + unsortedList.size());
            totalValue += thisValue;
            if(thisValue > 0) countedIterations++;

            info[0] = nextInfoChar(info[0]);

            if(info[0] == 'n') {
                info[1] = nextInfoChar(info[1]);

                if(info[1] == 'n') {
                    info[2] = nextInfoChar(info[2]);

                    if(info[2] == 'n') {
                        info[3] = nextInfoChar(info[3]);

                        if(info[3] == 'n') {
                            info[4] = nextInfoChar(info[4]);

                            if(info[4] == 'n') {
                                break;
                            }
                        }
                    }
                }
            }
        }

//        System.out.println("Scored word " + word + " as " + totalValue / countedIterations + " (with " + countedIterations + " counted iterations");
        return totalValue / (double) countedIterations;
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
