package org.example.poj.algorithm.base;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Solution {
    static final int[][] dirs = {{-1,0}, {1, 0}, {0, 1}, {0, -1}};

    // pos {1,1} rect {5, 5}
    List<int[]> neighbor(int[] pos, int[] rect) {
        int rows = rect[0], cols = rect[1];
        List<int[]> ans = new ArrayList<>();
        for (int[] dir : dirs) {
            int nx = dir[0] + pos[0];
            int ny = dir[1] + pos[1];
            if (nx < 0 || ny < 0 || nx > cols || ny > rows) { continue; }
            ans.add(dir);
        }
        return ans;
    }

    int[] findHighest(int[][] map, int[] rect) {
        int rows = rect[0], cols = rect[1];
        int max = 0;
        int[] pos = new int[] {0, 0};
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; ++j) {
                if(map[i][j] > max) {
                    max = map[i][j];
                    pos = new int[] {i, j};
                }
                max = Math.max(max, map[i][j]);
            }
        }
        return pos;
    }

    int skyboading(int[] rect, int[][] map) {
        int[] highest = findHighest(map, rect);
        PriorityQueue pq = new PriorityQueue();
        for (int[] n : neighbor(highest, rect)) {

        }

        return 0;
    }

    public static void main(String[] args) {
        Solution solution = new Solution();
        int[][] grid = new int[][] {
                {1, 2, 3, 4, 5},
                {1, 2, 3, 4, 5},
                {1, 2, 3, 4, 5},
                {1, 2, 3, 4, 5},
                {1, 2, 3, 4, 5}
        };
        int ans = solution.skyboading(new int[] {5, 5}, grid);
    }
}
