package org.example.poj.ac.one;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

public class Main01 {
    public static int action(int[] a, int[][] b) {
        int blen = b.length;
        int count = 0;
        for (int i = 0; i < blen; i++) {
            count++;
            int k = b[i][0];
            int v = b[i][1];
            if (a[k] == 0 || a[v] == 0) {
                count--;
            }
            a[k] = 0;
            a[v] = 0;
        }
        for (int i = 0; i < a.length; i++) {
            count = count + a[i];
        }
        return count;
    }

    public static String readString(StreamTokenizer st) throws IOException {
        st.nextToken();
        return st.sval;
    }

    public static int readInt(StreamTokenizer st) throws IOException {
        st.nextToken();
        return (int) st.nval;
    }
    public static void main(String[] args) throws IOException {
        StreamTokenizer st =new StreamTokenizer(new BufferedReader(new InputStreamReader(System.in)));
        int n = readInt(st);
        int m = readInt(st);
        int[] a = new int[n];
        int[][] b = new int[m][2];
        for (int i = 0; i < n; i++) {
            a[i] = 1;
        }
        for (int i= 0; i < m; i++) {
            for (int j = 0; j < 2; j++) {
                b[i][j] = readInt(st);
            }
        }
        System.out.println(action(a, b));
    }
}
