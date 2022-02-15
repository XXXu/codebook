package org.example.poj.leetcode.array;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Stack;

public class ArrayMerge {

    public int[][] merge(int[][] intervals) {
        Arrays.sort(intervals, Comparator.comparingInt(value -> value[0]));

        Stack<int[]> stack = new Stack<int[]>();

        for (int i = 0; i < intervals.length; i++) {
            int key2 = intervals[i][0];
            int val2 = intervals[i][1];
            if (stack.isEmpty()) {
                stack.push(intervals[i]);
            } else {
                int[] pop = stack.pop();
                int key1 = pop[0];
                int val1 = pop[1];
                if (val1 < key2) {
                    stack.push(pop);
                    stack.push(new int[]{key2, val2});
                } else if (val1 >= key2 && val1 <= val2) {
                    stack.push(new int[]{key1, val2});
                } else if (val1 >= key2 && val1 > val2) {
                    stack.push(new int[]{key1, val1});
                }
            }
        }

        int[][] merge = new int[stack.size()][2];

        for (int j = stack.size()-1; j >= 0; j--) {
            merge[j] = stack.pop();
        }

        return merge;
    }

    public static void main(String[] args) {
        int[][] intervals = {{1, 3}, {8, 10},{2, 6}, {15, 18}};
        ArrayMerge arrayMerge = new ArrayMerge();
        arrayMerge.merge(intervals);


    }
}
