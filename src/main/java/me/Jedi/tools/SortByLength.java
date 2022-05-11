package me.Jedi.tools;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

public class SortByLength {

    public void main(String[] args) throws IOException, URISyntaxException {
        InputStream words = this.getClass().getClassLoader().getResourceAsStream("all-words.txt");

        BufferedReader reader = new BufferedReader(new InputStreamReader(words));

        String st;

        List<String> wordList = new ArrayList<>();

        while((st = reader.readLine()) != null) {
            wordList.add(st);
        }

        reader.close();

        wordList.sort(Comparator.comparingInt(String::length));

        String path = "C:\\Users\\barta\\words-";
        File f = new File(path+"1.txt");
        FileWriter writer = new FileWriter(f);

        BufferedWriter out = new BufferedWriter(writer);

        int prevLength = 0;

        for(int i = 0; i< wordList.size(); i++) {
            String s = wordList.get(i);
            if(s.length() != prevLength) {
                f = new File(path + s.length() + ".txt");
                out.close();

                writer.close();
                writer = new FileWriter(f);

                out = new BufferedWriter(writer);
            }
            out.write(s);
            System.out.println("Printed " + s);

            if(wordList.size() - 1 >= (i+1)) {
                if (wordList.get(i+1).length() == s.length())  out.write("\n");
            }

            prevLength = s.length();
        }

        out.close();


    }
}
