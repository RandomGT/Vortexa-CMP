package com.vortexa.ui.page.post.detail

import org.json.JSONArray
import org.json.JSONObject

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
        val root = JSONObject(trimmed)
        if (!root.has("blocks")) return null
        val arr = root.getJSONArray("blocks")
        if (arr.length() == 0) return null
        parseBlocksArray(arr)
    } catch (_: Exception) {
        null
    }
}

private fun parseBlocksArray(arr: JSONArray): List<PostContentBlock>? {
    val out = mutableListOf<PostContentBlock>()
    for (i in 0 until arr.length()) {
        val o = arr.optJSONObject(i) ?: continue
        when (o.optString("type", "")) {
            "text" -> {
                val content = o.optString("content", "")
                val styleObj = o.optJSONObject("style")
                val bold = styleObj?.optBoolean("bold", false) ?: false
                val fontSize = styleObj?.let { s ->
                    val v = s.optInt("fontSize", -1)
                    if (v > 0) v else null
                }
                out.add(
                    PostContentBlock.Text(
                        content = content,
                        style = PostContentTextStyle(bold = bold, fontSizeSp = fontSize),
                    ),
                )
            }
            "image" -> {
                val url = o.optString("url", "")
                if (url.isBlank()) continue
                val w = o.optInt("width", 0).takeIf { it > 0 }
                val h = o.optInt("height", 0).takeIf { it > 0 }
                out.add(PostContentBlock.Image(url = url, width = w, height = h))
            }
            "video" -> {
                val url = o.optString("url", "")
                if (url.isBlank()) continue
                val cover = o.optString("cover", "").takeIf { it.isNotBlank() }
                out.add(PostContentBlock.Video(url = url, cover = cover))
            }
        }
    }
    return out.takeIf { it.isNotEmpty() }
}
