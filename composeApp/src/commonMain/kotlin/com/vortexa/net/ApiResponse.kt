package com.vortexa.net

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.longOrNull

data class ApiResponse(
    val code: Int,
    val message: String,
    val data: JsonElement? = null,
    val url: String? = null,
    val timestamp: Long = 0L
) {
    val isSuccess: Boolean get() = code == CODE_SUCCESS

    companion object {
        const val CODE_SUCCESS = 200
    }
}

internal fun JsonObject.toApiResponse(): ApiResponse {
    return ApiResponse(
        code = intValue("code") ?: -1,
        message = stringValue("message").orEmpty(),
        data = this["data"]?.takeUnless { it is JsonNull },
        url = stringValue("url"),
        timestamp = (this["timestamp"] as? JsonPrimitive)?.longOrNull ?: 0L
    )
}

internal fun JsonObject.stringValue(key: String): String? =
    (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content

internal fun JsonObject.intValue(key: String): Int? =
    (this[key] as? JsonPrimitive)?.content?.toIntOrNull()
