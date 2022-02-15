package org.example.poj.leetcode.array;

public class MiddleIndex {
    public int pivotIndex(int[] nums) {
        int count = 0;
        for (int i = 0; i < nums.length; i++) {
            count += nums[i];
        }
        int pivotCount = 0;
        for (int i = 0; i < nums.length; i++) {
            if ((pivotCount * 2 + nums[i]) == count) {
                return i;
            } else {
                pivotCount += nums[i];
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        int[] nums = {1, 7, 3, 6, 5, 6};
        int[] num1s = {1, 2, 3};
        int[] num2s = {2, 1, -1};
        MiddleIndex middleIndex = new MiddleIndex();
        System.out.println(middleIndex.pivotIndex(nums));
        System.out.println(middleIndex.pivotIndex(num1s));
        System.out.println(middleIndex.pivotIndex(num2s));
    }

}
