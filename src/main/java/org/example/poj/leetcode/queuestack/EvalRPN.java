package org.example.poj.leetcode.queuestack;

import java.util.Stack;

public class EvalRPN {
    // tokens = ["4","13","5","/","+"]
    public int evalRPN(String[] tokens) {
        Stack<String> stack = new Stack<>();
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.equals("+") || token.equals("-") || token.equals("/") || token.equals("*")) {
                String pop1 = stack.pop();
                String pop2 = stack.pop();
                int opration = opration(Integer.parseInt(pop1), Integer.parseInt(pop2), token);
                stack.push(String.valueOf(opration));
            } else {
                stack.push(token);
            }
        }
        return Integer.parseInt(stack.pop());
    }

    public int opration(int p1, int p2, String s) {
        if (s.equals("+")) {
            return p2 + p1;
        } else if (s.equals("-")) {
            return p2 - p1;
        } else if (s.equals("*")) {
            return p2 * p1;
        } else {
            return p2 / p1;
        }
    }
}
