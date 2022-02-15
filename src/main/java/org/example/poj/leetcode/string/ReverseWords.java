package org.example.poj.leetcode.string;

public class ReverseWords {
    public String reverseWords(String s) {
        String[] split = s.trim().split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = split.length - 1; i >= 0; i--) {
            if (!split[i].equals("")) {
                builder.append(split[i]);
                builder.append(" ");
            }
        }
        return builder.toString().trim();
    }

    public static void main(String[] args) {
        String s = "  Bob    Loves  Alice   ";
        ReverseWords reverseWords = new ReverseWords();
        System.out.println(reverseWords.reverseWords(s));
    }
}
