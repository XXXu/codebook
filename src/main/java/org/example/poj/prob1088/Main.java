package org.example.poj.prob1088;

import java.util.Scanner;

public class Main {
    public static int n = 0;
    public static int m = 0;
    public static int[][] a = new int[101][101];
    public static int[][] f = new int[100][100];

    public static void main(String[] args) {
        Scanner scanner=new Scanner(System.in);
        n=scanner.nextInt(); //row
        m=scanner.nextInt(); //clo

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                a[i][j] = scanner.nextInt();
                f[i][j] = 1;
            }
        }

        int ans=0;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                f[i][j] = get(i, j);
                ans = max(ans, f[i][j]);
            }
        }
        System.out.println(ans);
    }

    public static int max(int i, int j) {
        return i >= j ? i : j;
    }

    public static int get(int i, int j) {
        if (f[i][j] > 1) {
            return f[i][j];
        }

        int maxn = 1;
        int h = 0;
        // 左
        if(j-1>=0 && a[i][j]>a[i][j-1]) {
            h = get(i, j - 1) + 1;
            maxn = max(maxn, h);
        }
        // 右
        if(j+1<m && a[i][j]>a[i][j+1]) {
            h=get(i,j+1)+1;
            maxn=max(maxn,h);
        }
        // 上
        if(i-1>=0 && a[i][j]>a[i-1][j]) {
            h=get(i-1,j)+1;
            maxn=max(maxn,h);
        }
        // 下
        if(i+1<n && a[i][j]>a[i+1][j]) {
            h=get(i+1,j)+1;
            maxn=max(maxn,h);
        }
        return maxn;
    }

}
