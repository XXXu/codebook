package org.example.poj.leetcode.string;

public class LongestCommonPrefix {
    public String longestCommonPrefix(String[] strs) {
        StringBuilder builder = new StringBuilder();
        int max = strs[0].length();
        for (int j = 0; j < max; j++) {
            char c = strs[0].charAt(j);
            for (int i = 1; i < strs.length; i++) {
                String str = strs[i];
                if (str.length() > j) {
                    if (str.charAt(j) != c) {
                        return builder.toString();
                    }
                } else {
                    return builder.toString();
                }
            }
            builder.append(c);
        }
        return builder.toString();
    }

    public static void main(String[] args) {
//        String[] strs = {"flower", "flow", "flight"};
        String[] strs = {"ab","a"};
        LongestCommonPrefix commonPrefix = new LongestCommonPrefix();
        System.out.println(commonPrefix.longestCommonPrefix(strs));
    }
}
