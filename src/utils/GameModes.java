package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GameModes {
    private Mode mode;
    private List<String> words;
    private List<String> answers;
    String initialGuess;

    public GameModes(Mode mode, String words, String answer, String initialGuess) throws IOException {
        this.mode = mode;

        File guesses = new File("src/words/"+words+".txt");
        File answers = new File("src/words/"+answer+".txt");

        BufferedReader guessesReader = new BufferedReader(new FileReader(guesses));
        BufferedReader answersReader = new BufferedReader(new FileReader(answers));

        String st;

        this.words = new ArrayList<>();
        this.answers = new ArrayList<>();

        while((st = guessesReader.readLine()) != null) {
            this.words.add(st);
        }

        while((st = answersReader.readLine()) != null) {
            this.words.add(st);
            this.answers.add(st);
        }

        this.initialGuess = initialGuess;
    }

    public String getInitialGuess() {
        return initialGuess;
    }

    public Mode getMode() {
        return mode;
    }

    public List<String> getWords() {
        return words;
    }

    public List<String> getAnswers() {
        return answers;
    }


    public static enum Mode {
        Wordle, Wordle6, Absurdle
    }
}
