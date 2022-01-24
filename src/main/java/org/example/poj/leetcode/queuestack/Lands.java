package org.example.poj.leetcode.queuestack;

public class Lands {
    public int numIslands(char[][] grid) {
        int m = grid.length-1;
        int size = 0;
        for (int i = 0; i <= m; i++) {
            int n = grid[i].length-1;
            for (int j = 0; j <= n; j++) {
                if (grid[i][j] == '1') {
                    size++;
                    dfs(grid, i, j, m, n);
                }
            }
        }
        return size;
    }

    public void dfs(char[][] grid, int i, int j, int m, int n) {
        if (i < 0 || i > m || j < 0 || j > n || grid[i][j] == '0') {
            return;
        }
        grid[i][j] = '0';
        dfs(grid, i - 1, j, m, n);
        dfs(grid, i, j - 1, m, n);
        dfs(grid, i, j + 1, m, n);
        dfs(grid, i + 1, j, m, n);
    }

    public static void main(String[] args) {
        char[][] grid = {
                {'1', '1', '1', '1', '0'},
                {'1', '1', '0', '1', '0'},
                {'1', '1', '0', '0', '0'},
                {'0', '0', '0', '0', '0'}
        };

        char[][] grid1 = {
                {'1', '1', '0', '0', '0'},
                {'1', '1', '0', '1', '0'},
                {'0', '0', '1', '0', '0'},
                {'0', '0', '0', '1', '1'}
        };

        char[][] grid2 = {
                {'1', '1', '1', '0', '0'},
                {'1', '1', '0', '1', '0'},
                {'1', '1', '0', '0', '0'},
                {'0', '0', '0', '1', '1'}
        };

        char[][] grid3 = {
                {'0', '0', '1', '0', '0'},
                {'0', '1', '0', '1', '0'},
                {'0', '0', '1', '0', '0'},
                {'0', '0', '0', '0', '0'}
        };
        char[][] grid4 = {
                {'1'}
        };
        char[][] grid5 = {
                {'1','0','1','1','0','1','1'}
        };

        Lands lands = new Lands();
        System.out.println(lands.numIslands(grid2));

    }
}
