package org.example.test;

import org.apache.hadoop.io.Text;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

public class DataSortTest {
    public static void createData() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/root/test/data.txt")));
        String[] str = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (int j = 0; j < 1000000; j++) {
            for (int i = 0; i < 10; i++) {
                builder.append(str[random.nextInt(str.length)]);
            }
            writer.write(builder.toString());
            writer.newLine();
            builder.setLength(0);
        }
        writer.flush();
        writer.close();
    }

    public static void sort(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
//        Text[] texts = new Text[1000000];
        String[] texts = new String[1000000];
        String line = null;
        int i=0;
        while (!((line = reader.readLine()) == null)) {
            texts[i] = line;
            i++;
        }
        reader.close();
        long start = System.currentTimeMillis();
        Arrays.sort(texts);
        long end = System.currentTimeMillis();
        System.out.println("cost time(ms): "+(end-start));
    }

    public static void main(String[] args) throws IOException {
        String path = args[0];
        sort(new File(path));
    }
}
