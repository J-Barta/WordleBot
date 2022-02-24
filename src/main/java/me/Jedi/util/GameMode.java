package me.Jedi.util;

import me.Jedi.drivers.Driver;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GameMode {
    private List<WordData> words;
    private List<WordData> answers;
    String initialGuess;
    private Driver driver;

    public GameMode(String wordFile, String answerFile, String initialGuess, Driver driver) throws IOException {

        InputStream guesses = this.getClass().getClassLoader().getResourceAsStream(wordFile + ".txt");
        InputStream solutions = this.getClass().getClassLoader().getResourceAsStream(answerFile + ".txt");

        BufferedReader guessesReader = new BufferedReader(new InputStreamReader(guesses));
        BufferedReader answersReader = new BufferedReader(new InputStreamReader(solutions));

        String st;

        this.words = new ArrayList<>();
        this.answers = new ArrayList<>();
        this.driver = driver;

        while((st = guessesReader.readLine()) != null) {
            words.add(new WordData(st));
        }

        while((st = answersReader.readLine()) != null) {
            answers.add(new WordData(st));
        }

        //Add any answers that are not in the guesses list already
        for(int i = 0; i < answers.size(); i++) {
            if(!words.contains(answers.get(i)))  {
                words.add(answers.get(i));
            }
        }

        this.initialGuess = initialGuess;
    }

    public String getInitialGuess() {
        return initialGuess;
    }


    public List<WordData> getWords() {return words;}

    public List<WordData> getAnswers() {
        return answers;
    }

    public Driver getDriver() {
        return driver;
    }


}
