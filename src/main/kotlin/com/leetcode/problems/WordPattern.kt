package com.leetcode.problems

class WordPattern {
    fun wordPattern(pattern: String, s: String): Boolean {
        val words = s.split(' ')

        if (pattern.length != words.size) return false

        val patternToWord = mutableMapOf<Char, String>()
        val wordToPattern = mutableMapOf<String, Char>()

        val coded = pattern.mapIndexed { i, ch ->

            if (patternToWord.contains(ch)) {
                if (patternToWord[ch] == words[i]) ch else null
            } else {
                if (wordToPattern.contains(words[i])) null else {

                    patternToWord[ch] = words[i]
                    wordToPattern[words[i]] = ch
                    ch
                }
            }
        }

        return (pattern == coded.joinToString(""))
    }
}