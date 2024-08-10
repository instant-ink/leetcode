package com.leetcode.crawler

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class LeetCodeController(private val leetCodeService: LeetCodeService) {

    @GetMapping("/parse")
    fun parse(): String {
         leetCodeService.parseProblems()
        return "OK"
    }
}