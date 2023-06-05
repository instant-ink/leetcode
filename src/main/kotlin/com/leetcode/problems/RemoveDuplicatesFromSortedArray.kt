package com.leetcode.problems

class RemoveDuplicatesFromSortedArray {
    fun removeDuplicates(nums: IntArray): Int {

        var idx = 1
        for (i in 1..nums.size - 1) {

            if (nums[i - 1] != nums[i]) {
                nums[idx] = nums[i]
                idx++
            }

        }
        return idx
    }
}