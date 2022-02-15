package org.example.poj.leetcode.string;

public class FindMaxConsecutiveOnes {
    public int findMaxConsecutiveOnes(int[] nums) {
        int result = 0;
        int i = 0, j = 0;
        while (j < nums.length) {
            if (nums[j] == 1) {
                j++;
            } else {
                result = Math.max(result, j - i);
                j++;
                i = j;
            }
        }
        result = Math.max(result, j - i);
        return result;
    }

    public static void main(String[] args) {
        int[] nums = {1,1,0,1,1,1};
//        int[] nums = {1, 0, 1, 1, 0, 1};
        FindMaxConsecutiveOnes ones = new FindMaxConsecutiveOnes();
        System.out.println(ones.findMaxConsecutiveOnes(nums));
    }
}
