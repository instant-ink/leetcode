package com.leetcode.problems

class RepeatedSubstringPattern {
    fun repeatedSubstringPattern(s: String): Boolean {

        var i = 2;
        var found = false

        while (i <= s.length && !found) {

            if (s.length % i == 0) {

                val len = s.length / i
                val expected = s.substring(0, len)

                found = true;

                for (j in 1..i - 1) {
                    if (s.substring(j * len, (j + 1) * len) != expected)
                        found = false
                }
            }

            i++
        }

        return found
    }

}