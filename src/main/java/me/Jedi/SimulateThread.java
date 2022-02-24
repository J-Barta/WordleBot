package me.Jedi;

import me.Jedi.utils.ListModifiers;
import me.Jedi.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class SimulateThread extends Thread{
    private boolean finished;
    private List<GameData> games = new ArrayList<>();
    private List<GameData> failedGames=  new ArrayList<>();
    private List<String> gamesToPlay;
    private List<String> originalList;
    private List<String> unsortedAnswers;
    private double gamesPlayed;
    private String startingGuess;

    public SimulateThread(List<String> gamesToPlay, List<String> originalList, List<String> unsortedAnswers, String startingGuess)  {
        this.gamesToPlay = gamesToPlay;
        this.originalList = originalList;
        this.unsortedAnswers = unsortedAnswers;
        this.gamesPlayed = 0;
        this.startingGuess = startingGuess;
    }

    @Override
    public void run() {

        try {
            simulateGames();
            finished = true;
        }
        catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public double getGamesPlayed() {
        return  gamesPlayed;
    }

    public List<GameData> getGameData() {return games;}
    public List<GameData> getFailedGames() {return failedGames;}

    private void simulateGames() throws InterruptedException {

        for(String w : gamesToPlay) {
            List<String> wordListCopy = List.copyOf(originalList);
            List<String> answersCopy = List.copyOf(unsortedAnswers);

            Game game = new Game(wordListCopy, answersCopy, startingGuess);

            boolean success = false;

            while(game.getGuesses() < 6 && !success) {

                String guess = game.getNextGuess(false, true);
                String info;
                if(guess != null) info = getInfoFromWord(guess, w); //Get the info about the last guess
                else break;

                success = game.updateList(info);
                if(success) break;

                //Remove the available words based on the guess and the info
                wordListCopy = ListModifiers.updateList(guess, info, wordListCopy);
            }

            GameData thisGame = new GameData(success, game.getGuesses(), w);

            if(!success) {
                failedGames.add(thisGame);
            }

            games.add(thisGame);

            gamesPlayed++;
        }

        finished = true;
    }

    private String getInfoFromWord(String guess, String answer) {
        List<Character> answerList = Utils.stringToCharList(answer);

        List<Character> infoList = new ArrayList<>();

        for(int i = 0; i<guess.length(); i++) {
            Character c = guess.charAt(i);
            if(!answerList.contains(c)) infoList.add('n');
            else {
                if(rightIndex(c,i, answer)) {
                    infoList.add('g');
                } else {
                    infoList.add('y');
                }
            }
        }

        return Utils.charListToString(infoList);
    }

    private boolean rightIndex(Character c, Integer index, String answer) {
        int lowerBound = 0;
        while(answer.indexOf(c, lowerBound) != -1) {
            if(answer.indexOf(c, lowerBound) == index) return true;
            lowerBound = answer.indexOf(c, lowerBound) + 1;
        }

        return false;
    }

}
