package com.vortexa.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private val mediaListJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * 后端媒体字段在不同接口/版本中会返回数组、JSON 字符串或逗号分隔字符串。
 * 统一解析为 URL 列表，避免发帖、评论/回复图片提交成功但列表/详情展示为空。
 */
fun parseMediaUrlList(element: JsonElement?): List<String>? {
    if (element == null || element is JsonNull) return null
    return normalizeMediaUrls(mediaUrlsFromJsonElement(element)).takeIf { it.isNotEmpty() }
}

fun parseMediaUrlList(raw: String?): List<String> =
    normalizeMediaUrls(mediaUrlsFromString(raw?.trim().orEmpty()))

fun extractMediaUrlsFromContent(raw: String?): List<String> {
    val value = raw?.trim().orEmpty()
    if (value.isBlank()) return emptyList()

    val jsonUrls = if (value.startsWith("{") || value.startsWith("[")) {
        runCatching { mediaUrlsFromJsonElement(mediaListJson.parseToJsonElement(value)) }
            .getOrNull()
            .orEmpty()
    } else {
        emptyList()
    }
    return normalizeMediaUrls(jsonUrls + htmlImageUrls(value) + markdownImageUrls(value))
}

private fun mediaUrlsFromJsonElement(element: JsonElement): List<String> =
    when (element) {
        is JsonArray -> element.flatMap(::mediaUrlsFromJsonElement)
        is JsonObject -> mediaUrlsFromObject(element)
        is JsonPrimitive -> mediaUrlsFromString(element.content.trim())
    }

private fun mediaUrlsFromObject(obj: JsonObject): List<String> {
    for (key in listOf("url", "src", "mediaUrl", "imageUrl", "fileUrl", "path", "cover")) {
        val url = (obj[key] as? JsonPrimitive)?.content?.trim()
        if (!url.isNullOrEmpty()) return listOf(url)
    }
    for (key in listOf("urls", "mediaList", "images", "imageList", "blocks")) {
        val urls = mediaUrlsFromJsonElement(obj[key] ?: continue)
        if (urls.isNotEmpty()) return urls
    }
    return emptyList()
}

private fun mediaUrlsFromString(value: String): List<String> {
    if (value.isBlank() || value.equals("null", ignoreCase = true)) return emptyList()
    val parsed = if (value.startsWith("[") || value.startsWith("{")) {
        runCatching { mediaUrlsFromJsonElement(mediaListJson.parseToJsonElement(value)) }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }
    } else {
        null
    }
    if (parsed != null) return parsed

    return value
        .split(',')
        .map { it.trim().trim('"', '\'') }
}

private fun normalizeMediaUrls(urls: List<String>): List<String> {
    val out = mutableListOf<String>()
    val seen = mutableSetOf<String>()
    for (url in urls) {
        val normalized = url.trim().trim('"', '\'')
        if (normalized.isBlank() || normalized.equals("null", ignoreCase = true)) continue
        if (seen.add(normalized)) out.add(normalized)
    }
    return out
}

private fun htmlImageUrls(value: String): List<String> {
    val srcRegex = Regex("""(?i)<img\b[^>]*\bsrc\s*=\s*(?:"([^"]+)"|'([^']+)'|([^\s>]+))""")
    return srcRegex.findAll(value).mapNotNull { match ->
        (match.groups[1]?.value ?: match.groups[2]?.value ?: match.groups[3]?.value)
            ?.decodeBasicHtmlEntities()
    }.toList()
}

private fun markdownImageUrls(value: String): List<String> {
    val imageRegex = Regex("""!\[[^\]]*]\(([^)\s]+)(?:\s+"[^"]*")?\)""")
    return imageRegex.findAll(value).mapNotNull { it.groups[1]?.value }.toList()
}

private fun String.decodeBasicHtmlEntities(): String =
    replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&apos;", "'")
