package org.example.poj.algorithm.base;

public class MostGold {
    public static int getMostGold(int n, int w, int[] g, int[] p) {
        int col = w+1;
        int[] preResult = new int[col];
        int[] result = new int[col];
        for (int i = 0; i < col; i++) {
            if (i < p[0]) {
                preResult[i] = 0;
            } else {
                preResult[i] = g[0];
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < col; j++) {
                if (j < p[i]) {
                    result[j] = preResult[j];
                } else {
                    result[j] = Math.max(preResult[j], preResult[j - p[i]] + g[i]);
                }
            }
            for (int j = 0; j < col; j++) {
                preResult[j] = result[j];
            }
        }

        return result[w];
    }

    public static void main(String[] args) {
        int[] g = {400, 500, 200, 300, 350};
        int[] p = {5, 5, 3, 4, 3};
        System.out.println(getMostGold(5, 10, g, p));
    }
}
