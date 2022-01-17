package org.example.poj.algorithm.sort;

public class MergeSort {
    private static int[] b;
    public static void sort(int[] a) {
        b = new int[a.length];
        sort(a, 0, a.length-1);
    }

    private static void sort(int[] a, int low, int high) {
        if (high <= low) {
            return;
        }
        int mid = (low + high) / 2;
        sort(a, low, mid);
        sort(a, mid + 1, high);
        merge(a, low, mid, high);
    }

    private static void merge(int[] a, int low, int mid, int high) {
        for (int k = low; k <= high; k++) {
            b[k] = a[k];
        }
        int i = low;
        int j = mid + 1;
        for (int k = low; k <= high; k++) {
            if (i > mid) {
                a[k] = b[j];
                j++;
            } else if (j > high) {
                a[k] = b[i];
                i++;
            } else if (less(b[j],b[i])) {
                a[k] = b[j];
                j++;
            } else {
                a[k] = b[i];
                i++;
            }
        }

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

    public static boolean isSort(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (less(a[i], a[i - 1])) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        int[] a = {6, 3, 5, 1, 8, 2, 9, 10, 4};
        sort(a);
        show(a);
    }
}
