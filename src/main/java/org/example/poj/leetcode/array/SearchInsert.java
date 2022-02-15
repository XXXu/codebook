package org.example.poj.leetcode.array;

public class SearchInsert {
    public int searchInsert(int[] nums, int target) {
        int low = 0;
        int high = nums.length-1;
        int mid = 0;
        while (low <= high) {
            mid = (low + high) / 2;
            if (target < nums[mid]) {
                high = mid - 1;
            } else if (target > nums[mid]) {
                low = mid + 1;
            } else {
                return mid;
            }
        }
        return low;
    }

    public static void main(String[] args) {
        int[] nums = {1, 3};
        int target = 2;
        SearchInsert insert = new SearchInsert();
        System.out.println(insert.searchInsert(nums, target));
    }
}
