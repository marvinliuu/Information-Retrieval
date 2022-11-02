package org.example.lucene;

//import javax.swing.text.Document;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.*;
        import java.io.IOException;


public class preprocessing {

    //Split one big file into 1400 small files
    public static void main(String[] args) {
        try {
            BufferedReader file = new BufferedReader(new FileReader("data/cran.all.1400")); // Read the original big file
            String str;
            int n = 0;
            while ((str = file.readLine()) != null) {
                if (str.startsWith(".I")) n ++;
                File file1 = new File("data/cran_preprocess/cran" + n);
                if (!file1.exists()) file1.createNewFile();
                FileWriter fileWritter = new FileWriter("data/cran_preprocess/" + file1.getName(), true);
                fileWritter.write(str + "\n");
                fileWritter.close();
            }
        } catch (IOException e) {
            System.out.println("hello");
        }
    }
}




