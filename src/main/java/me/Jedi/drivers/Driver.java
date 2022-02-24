package me.Jedi.drivers;

import me.Jedi.utils.Utils;

import java.util.List;

public abstract class Driver {

    public void typeWord(String word) {
        List<Character> chars = Utils.stringToCharList(word);

        for(Character c : chars) {
            pressKey(c);
        }

        pressEnter();
    }

    public abstract void open();

    public abstract String getInfo(int guess) throws InterruptedException;

    protected abstract void pressKey(Character c);
    protected abstract void pressEnter();
}
