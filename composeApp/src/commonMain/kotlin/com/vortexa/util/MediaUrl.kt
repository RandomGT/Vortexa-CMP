package com.vortexa.util

import com.vortexa.config.AppConfig

fun resolveApiMediaUrl(url: String?): String? {
    val trimmed = url?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
    if (trimmed.startsWith("content://") || trimmed.startsWith("file://")) return trimmed
    if (trimmed.startsWith("data:")) return trimmed

    val baseUrl = AppConfig.API_BASE_URL.trimEnd('/').takeIf { it.isNotBlank() } ?: return trimmed
    return "$baseUrl/${trimmed.trimStart('/')}"
}

fun toImagePreviewUrls(values: List<Any?>): List<String> =
    values.mapNotNull { value -> resolveApiMediaUrl(value?.toString()) }

fun toImagePreviewUrls(values: List<Any?>, context: Any?): List<String> = toImagePreviewUrls(values)
