package org.example.poj.leetcode.string;

public class LongestPalindrome {
    public String longestPalindrome1(String s) {
        if (s.length() == 1) {
            return s;
        }
        int index = 0;
        int len = 1;
        for (int i = 0; i < s.length(); i++) {
            int i1 = longPalindrome(s, i, i);
            if (i1 > len) {
                len = i1;
                index = i - len / 2;
            }
            int i2 = longPalindrome(s, i, i + 1);
            if (i2 > len) {
                len = i2;
                index = i - (len / 2 - 1);
            }
        }
        return s.substring(index, index+len);
    }

    public int longPalindrome(String s, int left, int right) {
        while (left >= 0 && right < s.length()) {
            if (s.charAt(left) != s.charAt(right)) {
                break;
            }
            left--;
            right++;
        }
        return right - left - 1;
    }

    public static void main(String[] args) {
        /*LongestPalindrome longestPalindrome = new LongestPalindrome();
        String s = "cbbd";
        System.out.println(longestPalindrome.longestPalindrome1(s));*/

        int len = 5;

        int  index1 = 10 - (len / 2 - 1);
        int index2 = 10 - len / 2 - 1;
        System.out.println(index1);
        System.out.println(index2);
    }
}
