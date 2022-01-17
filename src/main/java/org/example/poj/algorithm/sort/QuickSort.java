package org.example.poj.algorithm.sort;

import java.util.Arrays;

public class QuickSort {
    public static void sort(int[] a) {
        sort(a, 0, a.length - 1);
    }

    private static void sort(int[] a, int low, int high) {
        if (high <= low) {
            return;
        }
        int partition = partition(a, low, high);
        sort(a, low, partition - 1);
        sort(a, partition + 1, high);
    }

    private static int partition(int[] a, int low, int high) {
        int i = low;
        int j = high + 1;
        int v = a[low];
        while (true) {
            while (less(a[++i], v)) {
                if (i == high) {
                    break;
                }
            }
            while (less(v, a[--j])) {
                if (j == low) {
                    break;
                }
            }
            if (j <= i) {
                break;
            }
            exch(a, i, j);
        }
        exch(a, low, j);
        return j;
    }

    private static boolean less(int i, int j) {
        return i < j;
    }

    private static void exch(int[] a, int i, int j) {
        int temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    private static void show(int[] a) {
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i]+"\t");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        int[] a = {6, 3, 5, 1, 8, 2, 9, 10, 4, 7};
        sort(a);
        show(a);
    }
}
