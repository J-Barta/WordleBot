package me.Jedi.util;

import me.Jedi.Main;

import java.util.*;

public class ListModifiers {
    /**
     * 'n' = not in the word; 'y' = wrong location in the word; 'g' = correct letter in correct location
     * @param info 5 characters that describe the previous guess
     * @param words
     * @return
     */
    public static List<WordData> updateList(String guess, String info, List<WordData> words) {
        List<WordData> correctedWords = words;
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
                correctedWords = removeWordsWithDupedChar(guessCharList.get(i), correctedWords, checkMap.size());
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
    public static List<WordData> removeLetter(Character letter, List<WordData> words) {
        List<WordData> correctedList = new ArrayList<>();
        for(WordData w : words) {
            if(!(w.getLetters().contains(letter))) correctedList.add(w);
        }
//
//        List<WordData> propertyList = Main.wordProperties.get(me.Jedi.scoring.WordProperties.LetterProperty.valueOf("I" + letter));
////        for(int i = 0; i< propertyList.size(); i++) words.remove(propertyList.get(i));
//
//        Iterator<WordData> itr= propertyList.iterator();
//
//        while(itr.hasNext()) {
//            WordData d = itr.next();
//            words.remove(d);
//        }

        return correctedList;

    }

    /**
     * Used to remove all words from the list if they don't have a specific letter (i.e. if the letter is yellow)
     * @param letter the letters that MUST be in the returned words
     * @param words the current list of words
     * @return the only remaining words that have the specified letter
     */
    public static List<WordData> removeWordsWithoutLetter(Character letter, List<WordData> words) {
        List<WordData> correctedList = new ArrayList<>();

        for(WordData w : words) {
            if(w.getLetters().contains(letter)) correctedList.add(w);
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
    public static List<WordData> removeWordsWithLetterAtIndex(Character c, Integer index, List<WordData> words) {

        //TOOD: Support duplicate letters in a word
        List<WordData> correctedList = new ArrayList<>();

        for(WordData w : words) {
            if(!w.getLetters().get(index).equals(c)) correctedList.add(w);
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
    public static List<WordData> removeWordsWithoutLetterAtIndex(Character c, Integer index, List<WordData> words) {
        //TOOD: Support duplicate letters in a word
        List<WordData> correctedList = new ArrayList<>();

        for(WordData w : words) {
            if(w.getLetters().get(index).equals(c)) correctedList.add(w);

        }

        return correctedList;
    }

    public static List<WordData> removeWordsWithDupedChar(Character c, List<WordData> words, int dupeCount) {
        List<WordData> correctedList = new ArrayList<>();

        for(WordData w : words) {
            if(instancesOf(c, w.getLetters()) < dupeCount) correctedList.add(w);
        }

        return correctedList;
    }

    private static int instancesOf(Character c, List<Character> list) {
        int count = 0;
        for(Character character : list) {
            if(character.equals(c)) count++;
        }
        return count;
    }
}
