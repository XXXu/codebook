package org.example.poj.ac.two;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

public class Main3 {
    public static int readInt(StreamTokenizer st) throws IOException {
        st.nextToken();
        return (int) st.nval;
    }

    public static void main(String[] args) throws IOException {
        StreamTokenizer st =new StreamTokenizer(new BufferedReader(new InputStreamReader(System.in)));
        int m = readInt(st);
        int[][] gongzuo = new int[m][2];
        int a = 0;
        int b = 0;
        int count = 0;
        int[][] tmp = new int[m-1][2];

        for (int i = 0; i < m; i++) {
            if (i == 0) {
                a = readInt(st);
                b = readInt(st);
                gongzuo[i][0] = a;
                gongzuo[i][1] = b;
            } else {
                gongzuo[i][0] = readInt(st);
                gongzuo[i][1] = readInt(st);
            }

        }
        int p=0;
        for (int i = 1; i < m; i++) {
            if (gongzuo[i][0] < a && gongzuo[i][1] > b) {
                p++;
                boolean fatal = false;
                for (int k = 0; k < tmp.length; k++) {
                    if (gongzuo[i][0] == tmp[k][0] && gongzuo[i][1] == tmp[k][1]) {
                        fatal = true;
                        break;
                    }
                }
                if (!fatal) {
                    count++;
                }
                tmp[p][0] = gongzuo[i][0];
                tmp[p][1] = gongzuo[i][1];
            }
        }
        System.out.println(count);
    }
}
