package com.vortexa.util

fun resolveApiMediaUrl(url: String?): String? = url?.takeIf { it.isNotBlank() }

fun toImagePreviewUrls(values: List<Any?>): List<String> =
    values.mapNotNull { value -> value?.toString()?.takeIf { it.isNotBlank() } }

fun toImagePreviewUrls(values: List<Any?>, context: Any?): List<String> = toImagePreviewUrls(values)
