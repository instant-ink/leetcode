package com.leetcode.problems

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WordPatternTest {

    private val problem = WordPattern()
    @Test
    fun wordPattern() {
        val result = problem.wordPattern("aabb", "hi hi my my")

        assertEquals(true, result)
    }
}