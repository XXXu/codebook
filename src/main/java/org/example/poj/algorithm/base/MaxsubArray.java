package org.example.poj.algorithm.base;

public class MaxsubArray {
    public static int maxSub(int[] a) {
        if (a == null || a.length == 0) {
            return 0;
        }
        int[] dp = new int[a.length];
        dp[0] = a[0];
        int max = dp[0];
        for (int i = 1; i < a.length; i++) {
            dp[i] = Math.max(dp[i - 1] + a[i], a[i]);
            max = Math.max(max, dp[i]);
        }
        for (int i = 0; i < a.length; i++) {
            System.out.print(dp[i] + "\t");
        }
        System.out.println();
        return max;
    }

    public static void main(String[] args) {
        int[] a = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        System.out.println(maxSub(a));
    }
}
