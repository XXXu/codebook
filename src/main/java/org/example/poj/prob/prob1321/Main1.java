package org.example.poj.prob.prob1321;

import java.util.Scanner;

public class Main1 {
    public static char a[][] = new char[10][10];     //记录棋盘位置
    public static int book[] = new int [10];        //记录一列是否已经放过棋子
    public static int n,k;
    public static int total,m;    //total 是放棋子的方案数 ，m是已放入棋盘的棋子数目

    public static void dfs(int cur) {
        if(k==m) {
            total++;
            return ;
        }
        if(cur>=n) { //边界
            return ;
        }
        for(int j=0; j<n; j++)
            if(book[j]==0 && a[cur][j]=='#') {//判断条件
                book[j]=1;           //标记
                m++;
                dfs(cur+1);
                book[j]=0;           //改回来方便下一行的判断
                m--;
            }
        dfs(cur+1);
    }

    public static void main(String[] args) {
        int i,j;
        Scanner scanner=new Scanner(System.in);
        n = scanner.nextInt();
        k = scanner.nextInt();
        while(n!=-1 && k!=-1) { //限制条件
            total=0;
            m=0;
            for(i=0; i<n; i++) {
                a[i][0] = scanner.next().charAt(0);
            }
            dfs(0);
            System.out.println(total);
        }
    }
}
