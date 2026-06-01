package com.vortexa.ui.page.post.detail

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject

/** 帖子详情正文「blocks」富文本中的单块内容（与接口 JSON 约定一致）。 */
sealed class PostContentBlock {
    data class Text(
        val content: String,
        val style: PostContentTextStyle = PostContentTextStyle(),
    ) : PostContentBlock()

    data class Image(
        val url: String,
        val width: Int?,
        val height: Int?,
    ) : PostContentBlock()

    data class Video(
        val url: String,
        val cover: String?,
    ) : PostContentBlock()
}

data class PostContentTextStyle(
    val bold: Boolean = false,
    val fontSizeSp: Int? = null,
)

/**
 * 若 [raw] 为 `{"blocks":[...]}` 且至少解析出一条块则返回列表；否则返回 null，调用方按普通字符串正文处理 。
 */
fun parsePostContentBlocksOrNull(raw: String): List<PostContentBlock>? {
    val trimmed = raw.trim()
    if (trimmed.length < 12 || !trimmed.startsWith("{")) return null
    return try {
        val root = Json.parseToJsonElement(trimmed).jsonObject
        val blocks = root["blocks"] as? JsonArray ?: return null
        if (blocks.isEmpty()) return null
        parseBlocksArray(blocks)
    } catch (_: Exception) {
        null
    }
}

private fun parseBlocksArray(blocks: JsonArray): List<PostContentBlock>? {
    val out = mutableListOf<PostContentBlock>()
    blocks.forEach { item ->
        val obj = item as? JsonObject ?: return@forEach
        when (obj.string("type")) {
            "text" -> {
                val style = obj["style"] as? JsonObject
                out.add(
                    PostContentBlock.Text(
                        content = obj.string("content").orEmpty(),
                        style = PostContentTextStyle(
                            bold = style?.boolean("bold") ?: false,
                            fontSizeSp = style?.positiveInt("fontSize"),
                        ),
                    ),
                )
            }
            "image" -> {
                val url = obj.string("url")?.takeIf { it.isNotBlank() } ?: return@forEach
                out.add(
                    PostContentBlock.Image(
                        url = url,
                        width = obj.positiveInt("width"),
                        height = obj.positiveInt("height"),
                    ),
                )
            }
            "video" -> {
                val url = obj.string("url")?.takeIf { it.isNotBlank() } ?: return@forEach
                out.add(
                    PostContentBlock.Video(
                        url = url,
                        cover = obj.string("cover")?.takeIf { it.isNotBlank() },
                    ),
                )
            }
        }
    }
    return out.takeIf { it.isNotEmpty() }
}

private fun JsonObject.string(key: String): String? =
    (this[key] as? JsonPrimitive)?.content

private fun JsonObject.boolean(key: String): Boolean? =
    (this[key] as? JsonPrimitive)?.booleanOrNull

private fun JsonObject.positiveInt(key: String): Int? =
    (this[key] as? JsonPrimitive)?.intOrNull?.takeIf { it > 0 }
