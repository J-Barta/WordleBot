import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class Utils {

    public static boolean switchToGuesses(String info) {
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
    public static List<String> updateList(String guess, String info, List<String> words) {
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
    public static List<String> removeLetter(Character letter, List<String> words) {
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
    public static List<String> removeWordsWithoutLetter(Character letter, List<String> words) {
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
    public static List<String> removeWordsWithLetterAtIndex(Character c, Integer index, List<String> words) {

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
    public static List<String> removeWordsWithoutLetterAtIndex(Character c, Integer index, List<String> words) {
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


    public static List<WordData> sortWordList(List<String> unsortedList, List<String> fullList, int id) {
        List<WordData> wordsWithWeight = new ArrayList<>();
        //Evaluate the score of each word
        System.out.print("Starting thread...");

        for(String s : unsortedList) {
            wordsWithWeight.add(new WordData(s, evaluateWord(s, fullList)));

            System.out.print('\r');
            System.out.print("Thread #" + id + " is " + ((unsortedList.indexOf(s) / (double) unsortedList.size()) * 100) + "% of the way done.");
        }

        //Sort the words by weight
        Collections.sort(wordsWithWeight, Comparator.comparingDouble(WordData::getScore));
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
            String correctedInfo = handleImpossibilities(word, charListToString(info));
            double thisValue = updateList(word, correctedInfo, unsortedList).size();
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
        List<Character> infoList = stringToCharList(info);

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

        return charListToString(infoList);
    }

    public static String charListToString(List<Character> chars) {
        String toReturn = "";

        for(Character c : chars) {
            toReturn = toReturn + c;
        }

        return toReturn;
    }

    public static String charListToString(Character[] chars) {
        return charListToString(List.of(chars));
    }

    public static List<Character> stringToCharList(String string) {
        List<Character> list = new ArrayList<>();

        for(int i = 0; i < string.length(); i++) {
            list.add(string.charAt(i));
        }

        return list;
    }

    public static Character nextInfoChar(Character c) {
        if(c == 'n') return 'y';
        else if(c == 'y') return 'g';
        else if(c == 'g') return 'n';

        return null;
    }
}

class InfoData {
    Character c;
    int index;

    public InfoData(Character c, int index) {
        this.c = c;
        this.index = index;
    }
}