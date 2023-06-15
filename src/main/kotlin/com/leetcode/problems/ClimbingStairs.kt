package com.leetcode.problems

class ClimbingStairs {
    fun climbStairs(n: Int): Int {
        if (n == 1) return 1

        if (n == 2) return 2

        val a = Array(n + 1) { 0 }
        a[0] = 0
        a[1] = 1
        a[2] = 2

        for (i in 3..n) {
            a[i] = a[i - 1] + a[i - 2]
        }

        return a[n]
    }
}