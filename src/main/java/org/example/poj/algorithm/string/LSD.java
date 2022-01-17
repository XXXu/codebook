package org.example.poj.algorithm.string;

public class LSD {
    public static void sort(String[] a, int w) {
        int n = a.length;
        int r = 256;
        String[] aux = new String[n];
        for (int d = w - 1; d >= 0; d--) {
            int[] count = new int[r + 1];
            for (int i = 0; i < n; i++) {
                char c = a[i].charAt(d);
                count[ c + 1]++;
            }
            for (int i = 0; i < r; i++) {
                count[i + 1] += count[i];
            }
            for (int i = 0; i < n; i++) {
                char c = a[i].charAt(d);
                int x = count[c]++;
                aux[x] = a[i];
            }
             for (int i = 0; i < n; i++) {
                a[i] = aux[i];
            }
        }
    }
    public static void main(String[] args) {
        String[] a = new String[13];
        a[0] = "4PGC938";
        a[1] = "2IYE230";
        a[2] = "3CI0720";
        a[3] = "1ICK750";
        a[4] = "1OHV845";
        a[5] = "4JZY524";
        a[6] = "1ICK750";
        a[7] = "3CI0720";
        a[8] = "1OHV845";
        a[9] = "1OHV845";
        a[10] = "2RLA629";
        a[11] = "2RLA629";
        a[12] = "3ATW723";
        sort(a, 7);
        for (int i = 0; i < a.length; i++) {
            System.out.println(a[i]);
        }
    }
}
