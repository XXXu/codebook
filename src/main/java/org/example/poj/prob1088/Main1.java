package org.example.poj.prob1088;

import java.util.Scanner;

public class Main1 {
    public static int[][] to = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {0,0}};
    public static int n, m;
    public static int[][] high = new int[105][105];
    public static int[][] maxLen = new int[105][105];

    public static void main(String[] args) {
        Scanner scanner=new Scanner(System.in);
        n = scanner.nextInt(); //row
        m = scanner.nextInt(); //clo

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                high[i][j] = scanner.nextInt();
                maxLen[i][j] = 0;
            }
        }

        int ans = 1;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                ans = max(ans, dfs(i, j));
            }
        }
        System.out.println(ans);
    }

    public static int dfs(int x, int y){
        if (maxLen[x][y] != 0) {
            return maxLen[x][y];
        }

        maxLen[x][y] = 1;
        for(int i = 0; i <= 4; i++){
            int x1 = x + to[i][0];
            int y1 = y + to[i][1];
            if(check(x1, y1) && high[x1][y1] < high[x][y]){
                maxLen[x][y] = max(dfs(x1, y1) + 1, maxLen[x][y]);
            }
        }
        return maxLen[x][y];
    }

    public static int max(int i, int j) {
        return i >= j ? i : j;
    }

    public static boolean check(int x, int y){
        if(x >= 1 && y >= 1 && x <= n && y <= m)
            return true;
        else{
            return false;
        }

    }

}
