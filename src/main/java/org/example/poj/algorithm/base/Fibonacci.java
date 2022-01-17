package org.example.poj.algorithm.base;

public class Fibonacci {
    public static long action(int n) {
        long[] a = new long[n + 1];
        a[0] = 1;
        a[1] = 1;
        if (n < 2) {
            return 1;
        }
        for (int i = 2; i <= n; i++) {
            a[i] = a[i - 1] + a[i - 2];
        }

        for (int i = 0; i <= n; i++) {
            System.out.print(a[i]+"\t");
        }
        System.out.println();
        return a[n];
    }
    public static void main(String[] args) {
        System.out.println(action(50));
    }
}
