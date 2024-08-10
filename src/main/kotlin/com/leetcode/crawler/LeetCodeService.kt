package com.leetcode.crawler

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.leetcode.crawler.problem.*
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.io.File

@Service
class LeetCodeService(private val restTemplate: RestTemplate, private val openAIService: OpenAIService) {

    fun parseProblems() {
        listOf(
            "string-compression",
            "increasing-triplet-subsequence",
            "product-of-array-except-self",
            "reverse-words-in-a-string",
            "reverse-vowels-of-a-string",
            "can-place-flowers",
            "kids-with-the-greatest-number-of-candies",
            "greatest-common-divisor-of-strings",
            "merge-strings-alternately"
        ).forEach { name ->
            val issue = try {
                parseIssue(name)
            } catch (e: Exception) {
                throw RuntimeException("Failed to parse$name", e)
            }

            createDirectory(name)
            saveToFile(issue.description, name, "description.md")
            saveToFile(
                jacksonObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(issue.copy(description = "description.md")),
                name,
                "problem.json"
            )

            Thread.sleep(1000)
        }
    }

    fun parseIssue(title: String): Problem {
        val summary = getSummary(title)
        val content = getQuestionContent(title)

        data class Param(
            val name: String,
            val type: String
        )

        data class Type(
            val type: String,
        )

        data class Output(
            val paramindex: Int,
            val size: String,
        )

        data class MetaData(
            val name: String,
            val params: List<Param>,
            val `return`: Type,
            val output: Output? = null
        )

        val metadata: MetaData = jacksonObjectMapper()
            .apply { configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) }
            .readValue(getMetadata(title).metaData, MetaData::class.java)

        val snippets = getSnippets(title)
        val tags = getTags(title)
        val similar = getSimilarQuestions(title)

        val problem = Problem(
            problemId = summary.titleSlug,
            name = summary.title,
            originalName = summary.title,
            number = summary.questionId.toInt(),
            description = content.content,
            difficulty = ProblemDifficulty.valueOf(summary.difficulty.uppercase()),
            snippets = snippets.codeSnippets.filter { langs.containsKey(it.langSlug) }
                .map { ProblemSnippet(language = langs.get(it.langSlug)!!, snippet = it.code, call = "") },
            cases = emptyList(),
            solution = null,
            metadata = metadata.let { m ->
                ProblemMetadata(
                    name = m.name,
                    params = m.params.map { MetadataParam(name = it.name, type = it.type) },
                    returnType = m.`return`.type,
                    outputParam = m.output?.paramindex
                )
            },
            tags = tags.topicTags.map { ProblemTag(tagId = it.slug, name = it.name) },
            similar = similar.similarQuestionList.map {
                SimilarProblem(
                    problemId = it.titleSlug,
                    name = it.title,
                    difficulty = ProblemDifficulty.valueOf(it.difficulty.uppercase())
                )
            }
        )

        return problem
    }

    val langs = mapOf(
        "csharp" to Language.CSHARP,
        "java" to Language.JAVA,
        "javascript" to Language.JS,
        "python3" to Language.PYTHON,
        "typescript" to Language.TS,
        "kotlin" to Language.KOTLIN,
        "onescript" to Language.ONESCRIPT
    )

    data class SimilarQuestions(
        val similarQuestionList: Array<SimilarQuestion>,
    )

    data class SimilarQuestion(
        val titleSlug: String,
        val title: String,
        val difficulty: String,
    )

    fun getSimilarQuestions(title: String): SimilarQuestions {
        val requestBody = "{\n" +
                "  \"query\": \"\\n    query SimilarQuestions(\$titleSlug: String!) {\\n  question(titleSlug: \$titleSlug) {\\n    similarQuestionList {\\n      difficulty\\n      titleSlug\\n      title\\n    }\\n  }\\n}\\n    \",\n" +
                "  \"variables\": {\n" +
                "    \"titleSlug\": \"$title\"\n" +
                "  },\n" +
                "  \"operationName\": \"SimilarQuestions\"\n" +
                "}"

        data class Data(val question: SimilarQuestions)
        data class GetResponse(val data: Data)
        return executeGraphQLQuery(requestBody, title, GetResponse::class.java).data.question
    }

    data class QuestionTags(
        val topicTags: Array<QuestionTag>,
    )

    data class QuestionTag(
        val name: String,
        val slug: String,
    )

    fun getTags(title: String): QuestionTags {
        val requestBody = "{\n" +
                "  \"query\": \"\\n    query singleQuestionTopicTags(\$titleSlug: String!) {\\n  question(titleSlug: \$titleSlug) {\\n    topicTags {\\n      name\\n      slug\\n    }\\n  }\\n}\\n    \",\n" +
                "  \"variables\": {\n" +
                "    \"titleSlug\": \"$title\"\n" +
                "  },\n" +
                "  \"operationName\": \"singleQuestionTopicTags\"\n" +
                "}"

        data class Data(val question: QuestionTags)
        data class GetResponse(val data: Data)
        return executeGraphQLQuery(requestBody, title, GetResponse::class.java).data.question
    }

    data class QuestionSnippets(
        val codeSnippets: Array<QuestionSnippet>,
    )

    data class QuestionSnippet(
        val lang: String,
        val langSlug: String,
        val code: String,
    )

    fun getSnippets(title: String): QuestionSnippets {
        val requestBody = "{\n" +
                "  \"query\": \"\\n    query questionEditorData(\$titleSlug: String!) {\\n  question(titleSlug: \$titleSlug) {\\n    codeSnippets {\\n      lang\\n      langSlug\\n      code\\n    }\\n  }\\n}\\n    \",\n" +
                "  \"variables\": {\n" +
                "    \"titleSlug\": \"$title\"\n" +
                "  },\n" +
                "  \"operationName\": \"questionEditorData\"\n" +
                "}"

        data class Data(val question: QuestionSnippets)
        data class GetResponse(val data: Data)
        return executeGraphQLQuery(requestBody, title, GetResponse::class.java).data.question
    }

    data class QuestionMetadata(
        val metaData: String,
    )

    fun getMetadata(title: String): QuestionMetadata {
        val requestBody = "{\n" +
                "  \"query\": \"\\n    query consolePanelConfig(\$titleSlug: String!) {\\n  question(titleSlug: \$titleSlug) {\\n    metaData\\n  }\\n}\\n\",\n" +
                "  \"variables\": {\n" +
                "    \"titleSlug\": \"$title\"\n" +
                "  },\n" +
                "  \"operationName\": \"consolePanelConfig\"\n" +
                "}"

        data class Data(val question: QuestionMetadata)
        data class GetResponse(val data: Data)
        return executeGraphQLQuery(requestBody, title, GetResponse::class.java).data.question
    }

    data class QuestionTitle(
        val questionId: String,
        val title: String,
        val titleSlug: String,
        val difficulty: String,
    )

    fun getSummary(title: String): QuestionTitle {
        val requestBody = "{\n" +
                "  \"query\": \"\\n    query questionTitle(\$titleSlug: String!) {\\n  question(titleSlug: \$titleSlug) {\\n    questionId\\n    title\\n    titleSlug\\n    difficulty\\n  }\\n}\\n    \",\n" +
                "  \"variables\": {\n" +
                "    \"titleSlug\": \"$title\"\n" +
                "  },\n" +
                "  \"operationName\": \"questionTitle\"\n" +
                "}"

        data class Data(val question: QuestionTitle)
        data class GetResponse(val data: Data)
        return executeGraphQLQuery(requestBody, title, GetResponse::class.java).data.question
    }

    data class QuestionContent(
        val content: String
    )

    fun getQuestionContent(title: String): QuestionContent {
        val requestBody = "{\n" +
                "  \"query\": \"query questionContent(\$titleSlug: String!) {question(titleSlug: \$titleSlug) {content mysqlSchemas dataSchemas }}\",\n" +
                "  \"variables\": {\n" +
                "    \"titleSlug\": \"$title\"\n" +
                "  },\n" +
                "  \"operationName\": \"questionContent\"\n" +
                "}"

        data class Data(val question: QuestionContent)
        data class GetResponse(val data: Data)
        return executeGraphQLQuery(requestBody, title, GetResponse::class.java).data.question
    }

    private fun <T> executeGraphQLQuery(requestBody: String, title: String, responseType: Class<T>): T {
        return restTemplate.exchange(
            "https://leetcode.com/graphql/",
            HttpMethod.POST,
            HttpEntity(requestBody, getLeetcodeHeaders()),
            responseType
        ).takeIf { it.statusCode == HttpStatus.OK }?.body
            ?: throw RuntimeException("Failed to retrieve data for: $title")
    }

    private fun getLeetcodeHeaders() =
        HttpHeaders().apply {
            add("accept", "*/*")
            add("accept-language", "en-US,en;q=0.9,ru-RU;q=0.8,ru;q=0.7")
            add("authorization", "")  // You may need to adjust this
            add(
                "baggage",
                "..."
            )
            add("content-type", "application/json")
            add(
                "cookie",
                "..."
            )
            add("origin", "https://leetcode.com")
            add("priority", "u=1, i")
            add("random-uuid", "7314993c-150a-8cdd-4935-5afe152e1d1b")
            add("referer", "https://leetcode.com/problems/contains-duplicate/description/")
            add("sec-ch-ua", "\"Not)A;Brand\";v=\"99\", \"Google Chrome\";v=\"127\", \"Chromium\";v=\"127\"")
            add("sec-ch-ua-mobile", "?0")
            add("sec-ch-ua-platform", "\"macOS\"")
            add("sec-fetch-dest", "empty")
            add("sec-fetch-mode", "cors")
            add("sec-fetch-site", "same-origin")
            add("sentry-trace", "883ad67770e44c5c82132accc1538b31-a12bb17396072775-0")
            add(
                "user-agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36"
            )
            add("x-csrftoken", "...")
        }

    fun saveToFile(text: String, directory: String, fileName: String) {
        val dir = File(directory)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val file = File(dir, fileName)
        file.writeText(text)
    }

    fun createDirectory(path: String): Boolean {
        val dir = File(path)
        return if (!dir.exists()) {
            dir.mkdir()
        } else {
            false
        }
    }
}