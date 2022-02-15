package org.example.poj.leetcode.array;

public class RotateMatrix {
    public void rotate(int[][] matrix) {
        int len = matrix.length;
        int maxIdx = len-1;
        int[][] newMatrix = new int[len][len];
        for(int y=0; y<len; y++){
            for(int x=0; x<len; x++){
                newMatrix[y][maxIdx-x]=matrix[x][y];
            }
        }
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                matrix[i][j] = newMatrix[i][j];
            }
        }
    }

    public void printMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                System.out.print(matrix[i][j] + "\t");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        int[][] ma = {
                {5, 1, 9, 11},
                {2, 4, 8, 10},
                {13, 3, 6, 7},
                {15, 14, 12, 16}
        };
        RotateMatrix rotateMatrix = new RotateMatrix();
        rotateMatrix.rotate(ma);
    }
}
