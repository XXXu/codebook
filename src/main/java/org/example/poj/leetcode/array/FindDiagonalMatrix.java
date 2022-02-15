package org.example.poj.leetcode.array;

public class FindDiagonalMatrix {
    public int[] findDiagonalOrder(int[][] mat) {
        int m = mat.length;
        int n = mat[0].length;
        int max = m + n - 2;
        int[] arr = new int[m * n];
        arr[0] = mat[0][0];
        int count = 1;
        for (int index = 1; index <= max; index++) {
            if (index % 2 == 0) {
                int i = index;
                int j = 0;
                while (i >= 0) {
                    if (i < m && j < n) {
                        int t = mat[i][j];
                        arr[count] = t;
                        count++;
                    }
                    i--;
                    j++;
                }
            } else {
                int i=0;
                int j = index;
                while (j >= 0) {
                    if (j < n && i < m) {
                        int t = mat[i][j];
                        arr[count] = t;
                        count++;
                    }
                    i++;
                    j--;
                }
            }
        }
        return arr;
    }

    public static void main(String[] args) {
//        int[][] matrix = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}, {13, 14, 15, 16}};
//        FindDiagonalMatrix diagonalMatrix = new FindDiagonalMatrix();
//        diagonalMatrix.findDiagonalOrder(matrix);
    }

}
