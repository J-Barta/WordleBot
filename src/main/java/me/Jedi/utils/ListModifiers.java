package me.Jedi.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListModifiers {
    /**
     * 'n' = not in the word; 'y' = wrong location in the word; 'g' = correct letter in correct location
     * @param info 5 characters that describe the previous guess
     * @param words
     * @return
     */
    public static List<String> updateList(String guess, String info, List<String> words) {
        List<String> correctedWords = words;
        List<Character> guessCharList = Utils.stringToCharList(guess);
        List<Character> infoList = Utils.stringToCharList(info);
        for(int i = 0; i<info.length(); i++) {
            Character c  = info.charAt(i);

            //Boolean that triggers whether special behvaior should occur (if there is a double letter in the guess but not in the word)
            boolean specialBehavior = false;

            int startIndex = 0;
            Map<Integer, Character> checkMap = new HashMap<>();
            while(guess.indexOf(guessCharList.get(i), startIndex) != -1) {
                int index = guess.indexOf(guessCharList.get(i), startIndex);
                checkMap.put(index, infoList.get(index));

                startIndex = index + 1;
            }

            //If there is a duplicate letter, we need to decide if special behavior is necessary
            if(checkMap.size() > 1) {
                if(
                    checkMap.containsValue('n') &&
                    (checkMap.containsValue('y') || checkMap.containsValue('g'))
                    && c == 'n'
                ) specialBehavior = true;
            }

            if(!specialBehavior) {
                if (c.equals('n')) {
                    correctedWords = removeLetter(guessCharList.get(i), correctedWords);
                } else if (c.equals('y')) {
                    correctedWords = removeWordsWithoutLetter(guessCharList.get(i), correctedWords);
                    correctedWords = removeWordsWithLetterAtIndex(guessCharList.get(i), i, correctedWords);
                } else if (c.equals('g')) {
                    correctedWords = removeWordsWithoutLetterAtIndex(guessCharList.get(i), i, correctedWords);
                }
            } else {
                correctedWords = removeWordsWithLetterAtIndex(guessCharList.get(i), i, correctedWords);
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
}
