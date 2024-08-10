package com.leetcode.crawler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.UnknownHttpStatusCodeException

@Service
class OpenAIService(private val restTemplate: RestTemplate) {

    private val apiUrl = "https://api.openai.com/v1/chat/completions"
    private val apiKey = "..."
    fun sendChatRequest(text: String): String {


        val prompt =
            "Translate the following LeetCode problem description to Russian, modify the provided examples with additional test cases, and convert the HTML format into Markdown format."

        val requestBody = mapOf(
            "model" to "gpt-4o-mini",
            "messages" to listOf(mapOf("role" to "user", "content" to "$prompt:\n$text"))
        )

        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $apiKey")
            set("Content-Type", "application/json")
        }

        val entity = HttpEntity(requestBody, headers)

        return try {
            val response: ResponseEntity<String> = restTemplate.exchange(
                apiUrl, HttpMethod.POST, entity, String::class.java
            )

            if (response.statusCode == HttpStatus.OK) {
                // Parse the response and extract the relevant data
                val mapper = jacksonObjectMapper()
                val responseMap: Map<String, Any> = mapper.readValue(response.body!!)
                val choices = responseMap["choices"] as List<Map<String, Any>>
                val message = choices.first()["message"] as Map<String, Any>
                message["content"] as String
            } else {
                "Error: Received status code ${response.statusCode}"
            }
        } catch (e: HttpClientErrorException) {
            "Error: ${e.statusCode} - ${e.message}"
        }
    }

}