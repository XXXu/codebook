package org.example.poj.leetcode.queuestack;

public class Test {
    public static void main(String[] args) {
        String str = "0000";
        System.out.println(str.substring(0, 0));
        System.out.println(str.substring(1));
        int i = 1;
        char ch = str.charAt(i);
        String strAdd = str.substring(0, i) + (ch == '9' ? 0 : ch - '0' + 1) + str.substring(i + 1);
        System.out.println(strAdd);
    }
}
