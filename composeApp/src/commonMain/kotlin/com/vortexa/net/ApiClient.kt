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
import io.ktor.client.request.get
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
import kotlinx.coroutines.CancellationException
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
            throw ApiException(-1002, networkErrorMessage(e), e)
        } catch (e: SerializationException) {
            throw ApiException(-1003, e.message ?: "Invalid response", e)
        } catch (e: IllegalArgumentException) {
            throw ApiException(-1004, e.message ?: "Invalid request", e)
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            throw ApiException(-1005, networkErrorMessage(e), e)
        }
    }

    suspend fun getJson(path: String, query: Map<String, Any?> = emptyMap()): ApiResponse {
        return try {
            val response = httpClient.get(resolveUrl(path, query))
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
            throw ApiException(-1002, networkErrorMessage(e), e)
        } catch (e: SerializationException) {
            throw ApiException(-1003, e.message ?: "Invalid response", e)
        } catch (e: IllegalArgumentException) {
            throw ApiException(-1004, e.message ?: "Invalid request", e)
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            throw ApiException(-1005, networkErrorMessage(e), e)
        }
    }

    private fun resolveUrl(path: String, query: Map<String, Any?> = emptyMap()): String {
        val builder = URLBuilder(AppConfig.API_BASE_URL)
        builder.appendPathSegments(path.trimStart('/').split('/'))
        query.forEach { (key, value) ->
            if (value != null) {
                builder.parameters.append(key, value.toString())
            }
        }
        return builder.buildString()
    }

    private fun networkErrorMessage(error: Throwable): String {
        val message = error.message.orEmpty()
        return when {
            message.contains("NSURLErrorDomain") ||
                message.contains("App Transport Security") ||
                message.contains("secure connection") -> "网络请求被 iOS 安全策略拦截，请检查接口 HTTPS/ATS 配置"
            message.contains("Unable to resolve host", ignoreCase = true) -> "网络不可用，请检查网络连接"
            message.contains("timeout", ignoreCase = true) -> "请求超时，请稍后重试"
            else -> message.ifBlank { "网络请求失败，请稍后重试" }
        }
    }
}
