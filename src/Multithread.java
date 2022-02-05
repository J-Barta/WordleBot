import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class Multithread extends Thread{
     List<String> toSort;
     List<WordData> wordsWithData = new ArrayList<>();
     private boolean finished = false;
    public Multithread(List<String> toSort) {
        super();
        this.toSort = toSort;
    }

    @Override
    public void run() {
        try {
            wordsWithData = Utils.sortWordList(toSort);
            System.out.println("Indicating the thread is finished");
            finished = true;
        }
        catch (Exception e) {
            System.out.println("Exception is caught");
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public List<WordData> getSortedList() {
        return wordsWithData;
    }
}
