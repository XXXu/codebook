package org.example.poj.algorithm.string;

public class Test {
    public static void main(String[] args) {
        int[] count = new int[257];
        count[10 + 1]++;
        count[10 + 1]++;
        System.out.println(count[10]);
        System.out.println(count[11]);

    }
}
