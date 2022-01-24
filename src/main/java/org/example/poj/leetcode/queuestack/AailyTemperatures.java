package org.example.poj.leetcode.queuestack;

import java.util.Stack;

public class AailyTemperatures {
    public int[] dailyTemperatures(int[] temperatures) {
        Stack<Integer> stack = new Stack<>();
        int[] temp = new int[temperatures.length];
        for (int i = 0; i < temp.length; i++) {
            while (!stack.empty() && temperatures[i] > temperatures[stack.peek()]) {
                Integer pop = stack.pop();
                temp[pop] = i - pop;
            }
            stack.push(i);
        }
        return temp;
    }
}
