package org.example.poj.leetcode.string;

public class MinSubArrayLen {
    public int minSubArrayLen(int target, int[] nums) {
        int slow = 0;
        int min = nums.length + 1;

        for (int i = 0; i < nums.length; i++) {
            if (toSum(nums, slow, i) >= target) {
                while (true) {
                    slow++;
                    if (toSum(nums, slow, i) < target) {
                        min = Math.min(min, i - slow + 2);
                        break;
                    }
                }
            }
        }
        return min > nums.length ? 0 : min;
    }

    public int toSum(int[] nums, int i, int j) {
        int sum = 0;
        for (; i <= j; i++) {
            sum = sum + nums[i];
        }
        return sum;
    }

    public static void main(String[] args) {
        int[] nums = {2, 3, 1, 2, 4, 3};
        int target = 7;
        MinSubArrayLen minSubArrayLen = new MinSubArrayLen();
        System.out.println(minSubArrayLen.minSubArrayLen(target, nums));
    }
}
