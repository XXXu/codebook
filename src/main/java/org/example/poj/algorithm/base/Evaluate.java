package org.example.poj.algorithm.base;

import java.util.Scanner;
import java.util.Stack;

public class Evaluate {
    // 1+(2+3)*4*5
    // 1+(2+3)*(4*5)
    // 1+((2+3)*(4*5))
    // (1+((2+3)*(4*5)))
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String next = scanner.next();
        Stack<Integer> vals = new Stack<>();
        Stack<String> opts = new Stack<>();
        for (int i = 0; i < next.length() - 1; i++) {
            char c = next.charAt(i);
            if ('+' == c || '-' == c || '*' == c || '/' == c) {
                opts.push(Character.toString(c));
            } else if ('('==c){

            } else if (')' == c) {
                String s = opts.pop();
                Integer p1 = vals.pop();
                Integer p2 = vals.pop();
                vals.push(opration(p1,p2,s));
            } else {
                vals.push(Character.getNumericValue(c));
            }
        }
        System.out.println(opration(vals.pop(),vals.pop(),opts.pop()));
    }

    public static int opration(int p1, int p2, String s) {
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
