package me.Jedi.utils;

import java.util.ArrayList;
import java.util.List;


public class Utils {
    /**
     * @param info the clue given by the last guess
     * @return true if we should switch to using only answer words instead of evaluating all words
     */
    public static boolean switchToAnswers(String info) {
        double correctPercentage = 0;
        for(int i = 0; i < info.length(); i++) {
            if(info.charAt(i) == 'y') correctPercentage += 10;
            if(info.charAt(i) == 'g') correctPercentage += 20;
        }

        return correctPercentage >= 40;
    }


    public static String charListToString(List<Character> chars) {
        String toReturn = "";

        for(Character c : chars) {
            toReturn = toReturn + c;
        }

        return toReturn;
    }

    public static String charListToString(Character... chars) {
        return charListToString(List.of(chars));
    }

    public static List<Character> stringToCharList(String string) {
        List<Character> list = new ArrayList<>();

        for(int i = 0; i < string.length(); i++) {
            list.add(string.charAt(i));
        }

        return list;
    }
}