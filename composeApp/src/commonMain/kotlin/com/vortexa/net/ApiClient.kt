package com.vortexa.net

import com.vortexa.config.AppConfig
import com.vortexa.config.TokenConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

object ApiClient {
    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    val httpClient: HttpClient = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 15_000
            requestTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            val token = TokenConfig.getToken()
            if (token.isNotEmpty()) {
                header(HttpHeaders.Authorization, token)
            }
        }
    }

    suspend fun postJson(path: String, body: JsonObject): ApiResponse {
        return try {
            val response = httpClient.post(resolveUrl(path)) {
                setBody(body)
            }
            if (response.status != HttpStatusCode.OK) {
                throw ApiException(response.status.value, response.bodyAsText().ifBlank { response.status.description })
            }
            response.body<JsonObject>().toApiResponse().also {
                if (!it.isSuccess) {
                    throw ApiException(it.code, it.message)
                }
            }
        } catch (e: ApiException) {
            throw e
        } catch (e: HttpRequestTimeoutException) {
            throw ApiException(-1001, "Request timeout", e)
        } catch (e: IOException) {
            throw ApiException(-1002, e.message ?: "Network unavailable", e)
        } catch (e: SerializationException) {
            throw ApiException(-1003, e.message ?: "Invalid response", e)
        } catch (e: IllegalArgumentException) {
            throw ApiException(-1004, e.message ?: "Invalid request", e)
        }
    }

    private fun resolveUrl(path: String): String {
        val builder = URLBuilder(AppConfig.API_BASE_URL)
        builder.appendPathSegments(path.trimStart('/').split('/'))
        return builder.buildString()
    }
}
