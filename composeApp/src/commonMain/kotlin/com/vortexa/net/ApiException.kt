package com.vortexa.net

class ApiException(
    val code: Int,
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message ?: "Request failed: $code", cause)
