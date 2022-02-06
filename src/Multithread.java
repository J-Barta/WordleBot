import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class Multithread extends Thread{
     List<String> toSort;
    List<String> fullList;
    int id;

    List<WordData> wordsWithData = new ArrayList<>();
     private boolean finished = false;
    public Multithread(List<String> toSort, List<String> fullList, int id) {
        super();
        this.toSort = toSort;
        this.fullList = fullList;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            wordsWithData = Utils.sortWordList(toSort, fullList, id);
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
