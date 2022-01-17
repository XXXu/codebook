package org.example.poj.prob2181;

import java.util.Scanner;

public class Main {
    public static final int N = 200000;
    public static int[] sum = new int[N];
    public static int[][] dp = new int[N][2];
    public static int n = 0;

    public static void main(String[] args) {
        Scanner scanner=new Scanner(System.in);
        n = scanner.nextInt();
        for (int i = 1; i <= n; i++) {
            sum[i] = scanner.nextInt();
        }

        for (int i = 1; i <= n; i++) {
            dp[i][0] = max(dp[i - 1][0], dp[i - 1][1] + sum[i]);
            dp[i][1] = max(dp[i - 1][1], dp[i - 1][0] - sum[i]);
        }
        System.out.println(max(dp[n][0], dp[n][1]));

    }

    public static int max(int i, int j) {
        return i >= j ? i : j;
    }
}
