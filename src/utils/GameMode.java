package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GameMode {
    private List<String> words;
    private List<String> answers;
    String initialGuess;

    public GameMode(String wordFile, String answerFile, String initialGuess) throws IOException {
        File guesses = new File("src/words/"+wordFile+".txt");
        File solutions = new File("src/words/"+answerFile+".txt");

        BufferedReader guessesReader = new BufferedReader(new FileReader(guesses));
        BufferedReader answersReader = new BufferedReader(new FileReader(solutions));

        String st;

        this.words = new ArrayList<>();
        this.answers = new ArrayList<>();

        while((st = guessesReader.readLine()) != null) {
            words.add(st);
        }

        while((st = answersReader.readLine()) != null) {
            answers.add(st);
        }

        //Add any answers that are not in the guesses list already
        for(int i = 0; i< answers.size(); i++) {
            if(!words.contains(answers.get(i))) words.add(answers.get(i));
        }

        this.initialGuess = initialGuess;
    }

    public String getInitialGuess() {
        return initialGuess;
    }


    public List<String> getWords() {
        return words;
    }

    public List<String> getAnswers() {
        return answers;
    }


}
