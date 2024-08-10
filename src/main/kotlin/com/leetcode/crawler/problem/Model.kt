package com.leetcode.crawler.problem

import com.fasterxml.jackson.annotation.JsonValue

data class Problem(
    val problemId: String,
    val name: String,
    val originalName: String,
    val number: Int,
    val description: String,
    val difficulty: ProblemDifficulty,
    val snippets: List<ProblemSnippet>,
    val cases: List<ProblemCase>,
    val solution: ProblemSolution?,
    val metadata: ProblemMetadata,
    val tags: List<ProblemTag>,
    val similar: List<SimilarProblem>
)

data class ProblemSnippet(
    val language: Language,
    val snippet: String,
    val call: String,
)

data class ProblemCase(
    val input: String,
    val expected: String,
)

data class ProblemSolution(
    val problemId: String,
    val language: Language,
    val snippet: String,
)

data class ProblemMetadata(
    val name: String,
    val params: List<MetadataParam>,
    val returnType: String,
    val outputParam: Int?
)

data class MetadataParam(
    val name: String,
    val type: String,
)

data class ProblemTag(
    val tagId: String,
    val name: String,
)

data class SimilarProblem(
    val problemId: String,
    val name: String,
    val difficulty: ProblemDifficulty
)

enum class ProblemDifficulty {
    EASY, MEDIUM, HARD
}

enum class Language(private val id: String) {
    CSHARP("csharp"),
    JAVA("java"),
    JS("js"),
    PYTHON("python"),
    TS("ts"),
    KOTLIN("kotlin"),
    ONESCRIPT("onescript");

    @JsonValue
    fun getId(): String = id
}