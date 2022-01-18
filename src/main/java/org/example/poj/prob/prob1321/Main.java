package org.example.poj.prob.prob1321;

import java.util.Scanner;

public class Main {
    public static final int max = 10;
    public static boolean board[][] = new boolean[10][10];
    public static boolean row[] = new boolean[max];
    public static boolean col[] = new boolean[max];
    public static int len, num, ans;
    public static void dfs(int depth, int start_line){
        if(depth == num){
            ans ++;
            return;
        }
        for(int i = start_line + 1; i <= len; i ++) {
            for(int j = 1; j <= len; j ++) {
                if(board[i][j] == true && row[i] == false && col[j] == false){
                    row[i] = true;  col[j] = true;
                    dfs(depth + 1, i);
                    row[i] = false;  col[j] = false;
                }
            }
        }

    }

    public static void main(String[] args) {
        int i, j;
        char c;
        Scanner scanner=new Scanner(System.in);
        len = scanner.nextInt();
        num = scanner.nextInt();
        while (len!=-1 && num!=-1) {
            for(i = 1; i <= len; i ++) {
                for(j = 1; j <= len; j ++){
                    c = scanner.next().charAt(0);
                    if(c == '#') {
                        board[i][j] = true;
                    }
                }
            }
            ans = 0;
            int start_line = len - num + 1; // 第一颗棋子的位置可能在 1 ~ start_line 行之间。
            for(i = 1; i <= start_line; i ++)
                for(j = 1; j <= len; j ++)
                    if(board[i][j] == true){
                        row[i] = true;  col[j] = true;
                        dfs(1, i);
                        row[i] = false;  col[j] = false;
                    }
            System.out.println(ans);
        }
    }

}
