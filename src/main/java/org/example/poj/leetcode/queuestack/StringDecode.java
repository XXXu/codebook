package org.example.poj.leetcode.queuestack;

import java.util.Stack;

public class StringDecode {
    public String decodeString(String s) {
        Stack<Character> allstack = new Stack<Character>();
        Stack<Character> stack = new Stack<Character>();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ']') {
                StringBuilder charBuild = new StringBuilder();
                Character item = null;
                while ((item=allstack.pop())!='[') {
                    charBuild.append(item);
                }
                String ziStr = charBuild.reverse().toString();
                charBuild.setLength(0);

                while (!allstack.isEmpty()) {
                    item = allstack.pop();
                    if (Character.isDigit(item)) {
                        charBuild.append(item);
                    } else {
                        allstack.push(item);
                        break;
                    }
                }
                StringBuilder ziBuild = new StringBuilder();
                Integer ziShu = Integer.parseInt(charBuild.reverse().toString());
                for (int j = 0; j < ziShu; j++) {
                    ziBuild.append(ziStr);
                }
                String zi = ziBuild.toString();
                for (int j = 0; j < zi.length(); j++) {
                    allstack.push(zi.charAt(j));
                }
            } else {
                allstack.push(c);
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        while (!allstack.isEmpty()) {
            stringBuilder.append(allstack.pop());
        }
        return stringBuilder.reverse().toString();
    }

    public static void main(String[] args) {
        String s = "3[a]2[bc]";
        String s1 = "3[a20[abc]]";
        String s2 = "100[leetcode]";
        StringDecode decode = new StringDecode();
        System.out.println(decode.decodeString(s1));
    }
}
