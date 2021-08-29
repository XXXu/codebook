package org.example.poj.prob1850;

import java.util.Scanner;

public class Main {
    public static int[][] c =new int[27][27];
    public static void main(String[] args) {
        initArry();
        Scanner scanner=new Scanner(System.in);
        String str = scanner.next();
        char[] strArr = str.toCharArray();
        int len = str.length();
        boolean flag = true;
        for (int i = 1; i < len; i++) {
            if (strArr[i - 1] >= strArr[i]) {
                flag = false;
                break;
            }
        }
        if (!flag) {
            System.out.println(0);
            return;
        }
        int ans = 0;
        for (int i = 1; i < len; i++) {
            ans += c[26][i];
        }

        int ch,ch1;
        for (int i = 0; i < len; i++) {
            ch = strArr[i] - 'a';
            if (i == 0) {
                ch1 = 0;
            } else {
                ch1 = strArr[i - 1] - 'a' + 1;
            }
            while (ch > ch1) {
                ans += c[26 - ch][len - 1 - i];
                ch--;
            }
        }
        ans++;
        System.out.println(ans);
    }

    public static void initArry() {
        c[0][0] = c[1][0] = c[1][1] = 1;
        for (int i = 2; i <= 26; i++) {
            c[i][0] = c[i][i] = 1;
            for (int j = 1; j < i; j++) {
                c[i][j] = c[i-1][j] + c[i-1][j-1];
            }
        }
    }
}
