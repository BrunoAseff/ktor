package com.example.ktor

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class PostService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
        }
    }

    suspend fun getPosts(page: Int, limit: Int, userId: Int? = null): List<Post> {
        val response = client.get("https://jsonplaceholder.typicode.com/posts") {
            parameter("_page", page)
            parameter("_limit", limit)
            if (userId != null) {
                parameter("userId", userId)
            }
        }
        
        return when (response.status.value) {
            in 200..299 -> response.body()
            404 -> throw Exception("Postagens não encontradas (404)")
            500 -> throw Exception("Erro interno no servidor (500)")
            else -> throw Exception("Erro inesperado: ${response.status.value}")
        }
    }
}
