public class GameData {
    private boolean success;
    private int guesses;
    private String answer;

    public GameData(boolean success, int guesses, String answer) {
        this.success = success;
        this.guesses = guesses;
        this.answer = answer;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getGuesses() {
        return guesses;
    }

    public String getAnswer() {
        return answer;
    }
}
