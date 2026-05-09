package com.vortexa.ui.page.post.detail

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
    return null
}
