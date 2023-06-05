package com.leetcode.problems

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RemoveDuplicatesFromSortedArrayTest {

    @Test
    fun removeDuplicates() {
        assertEquals(2, RemoveDuplicatesFromSortedArray().removeDuplicates(intArrayOf(1, 1, 2)))
    }
}