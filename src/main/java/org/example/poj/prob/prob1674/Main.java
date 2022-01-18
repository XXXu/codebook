package org.example.poj.prob.prob1674;

import java.util.Scanner;

public class Main {
    public static void swap(int[] a, int i, int j) {
        int b = a[i];
        a[i] = a[j];
        a[j] = b;
    }
    public static void main(String[] args) {
        int n;
        int swap_times;
        Scanner scanner=new Scanner(System.in);
        int t = scanner.nextInt();
        while (t > 0) {
            swap_times = 0;
            int[] a = new int[10005];
            n = scanner.nextInt();
            for (int i = 0; i < n; i++) {
                a[i] = scanner.nextInt();
            }

            for (int i = 0; i < n; i++) {
                if (a[i] != i + 1) {
                    for (int j = i + 1; j < n; j++) {
                        if (a[j] == i + 1) {
                            swap(a, i, j);
                            swap_times++;
                            break;
                        }
                    }
                }
            }
            System.out.println(swap_times);
            t--;
        }
    }
}
