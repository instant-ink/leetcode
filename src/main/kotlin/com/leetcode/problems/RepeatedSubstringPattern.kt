package com.leetcode.problems

class RepeatedSubstringPattern {
    fun repeatedSubstringPattern(s: String): Boolean {
        var i = 2;
        var found = false

        while (i <= s.length && !found) {
            if (s.length % i == 0) {
                found = s.substring(0, s.length / i).repeat(i) == s
            }
            i++
        }
        return found
    }
}