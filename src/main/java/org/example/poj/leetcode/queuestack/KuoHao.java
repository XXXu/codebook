package org.example.poj.leetcode.queuestack;

import java.util.Stack;

public class KuoHao {
    //()[]{}
    public boolean isValid(String s) {
        if (s.length() % 2 == 1) {
            return false;
        }
        Stack<Character> stack = new Stack<Character>();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else {
                if (stack.empty()) {
                    return false;
                }
                Character pop = stack.pop();
                if (c == ')') {
                    if (pop != '(') {
                        return false;
                    }
                }
                if (c == ']') {
                    if (pop != '[') {
                        return false;
                    }
                }
                if (c == '}') {
                    if (pop != '{') {
                        return false;
                    }
                }
            }
        }
        if (stack.empty()) {
            return true;
        } else {
            return false;
        }
    }
}
