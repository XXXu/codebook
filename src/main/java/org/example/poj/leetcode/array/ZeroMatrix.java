package org.example.poj.leetcode.array;

import java.util.Stack;

public class ZeroMatrix {
    public class Node {
        private int i;
        private int j;

        public Node(int i,int j) {
            this.i = i;
            this.j = j;
        }
    }
    public void setZeroes(int[][] matrix) {
        int hlen = matrix.length;
        int llen = matrix[0].length;
        Stack<Node> stack = new Stack<>();
        for (int i = 0; i < hlen; i++) {
            for (int j = 0; j < llen; j++) {
                if (matrix[i][j] == 0) {
                    stack.push(new Node(i, j));
                }
            }
        }
        while (!stack.isEmpty()) {
            Node pop = stack.pop();
            Integer key = pop.i;
            Integer value = pop.j;
            for (int i = 0; i < hlen; i++) {
                matrix[i][value] = 0;
            }
            for (int j = 0; j < llen; j++) {
                matrix[key][j] = 0;
            }
        }
    }

    public static void main(String[] args) {
        int[][] matrix= {{0,1,2,0},{3,4,5,2},{1,3,1,5}};
        ZeroMatrix zeroMatrix = new ZeroMatrix();
        zeroMatrix.setZeroes(matrix);
    }
}
