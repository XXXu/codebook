package org.example.poj.algorithm.base;

public class BinarySearch {
    public static void main(String[] args) {
        int[] a = {1, 4, 6, 8, 9, 10, 14, 16, 20};
        int num = 3;
        System.out.println(a);
//        System.out.println(binarySearch(num, a, 0, a.length - 1));
        System.out.println(binarySearchWhile(num,a));
    }

    public static int binarySearchWhile(int num, int[] a) {
        int low = 0;
        int high = a.length-1;
        int mid = (low + high) / 2;
        while (num != a[mid] && low < high) {
            if (num < a[mid]) {
                high = mid -1;
            } else if (num > a[mid]) {
                low = mid +1;
            }
            mid = (low + high) / 2;
        }
        if (low >= high) {
            return -1;
        } else {
            return mid;
        }

    }

    public static int binarySearch(int num, int[] a, int low, int high) {
        if (low < high) {
            int mid = (low + high) / 2;
            if (num < a[mid]) {
                return binarySearch(num, a, low, mid - 1);
            } else if (num > a[mid]) {
                return binarySearch(num, a, mid + 1, high);
            } else {
                return mid;
            }
        } else {
            return -1;
        }

    }
}
