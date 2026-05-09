package com.vortexa.ui.page.post.detail

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.vortexa.ui.theme.Colors
import com.vortexa.util.appendWithUrlSpans

/**
 * 内联话题：一个 Tag 为「以 `#` 开始」，话题名为 `#` 后连续的非空白、非 `#` 字符；
 * **以空白（空格、换行等）或正文结束** 作为该 Tag 的结束边界（尾随空白不参与高亮）。
 */
internal val INLINE_TOPIC_REGEX =
    Regex("#[^\\s#]{1,50}(?=\\s|\$)")

/** 正文片段：链接 + 话题样式（不含尾部标签与免责声明）。 */
internal fun buildPostBodySegmentAnnotatedString(content: String): AnnotatedString =
    buildAnnotatedString {
        appendWithUrlSpans(content, onPlain = { appendContentWithTopicStyle(it) })
    }

/** 完整帖子正文：正文 + 内联标签 + 免责声明。 */
internal fun buildPostContentAnnotatedString(
    content: String,
    inlineTags: List<String>,
    disclaimer: String?,
) = buildAnnotatedString {
    appendWithUrlSpans(content, onPlain = { appendContentWithTopicStyle(it) })
    if (inlineTags.isNotEmpty()) {
        append(" ")
        inlineTags.forEachIndexed { index, tag ->
            appendTopicTag(tag)
            if (index < inlineTags.size - 1) append(" ")
        }
    }
    disclaimer?.let {
        append(" ")
        withStyle(SpanStyle(color = Colors.gray_B1B8C6, fontSize = 12.sp)) {
            append(it)
        }
    }
}

/** 富文本 blocks 之后的尾部：内联标签 + 免责声明。 */
internal fun buildPostTailAnnotatedString(
    inlineTags: List<String>,
    disclaimer: String?,
): AnnotatedString = buildAnnotatedString {
    if (inlineTags.isNotEmpty()) {
        append(" ")
        inlineTags.forEachIndexed { index, tag ->
            appendTopicTag(tag)
            if (index < inlineTags.size - 1) append(" ")
        }
    }
    disclaimer?.let {
        append(" ")
        withStyle(SpanStyle(color = Colors.gray_B1B8C6, fontSize = 12.sp)) {
            append(it)
        }
    }
}

internal fun AnnotatedString.Builder.appendContentWithTopicStyle(content: String) {
    var currentIndex = 0
    INLINE_TOPIC_REGEX.findAll(content).forEach { match ->
        if (match.range.first > currentIndex) {
            append(content.substring(currentIndex, match.range.first))
        }
        appendTopicTag(match.value)
        currentIndex = match.range.last + 1
    }
    if (currentIndex < content.length) {
        append(content.substring(currentIndex))
    }
}

internal fun AnnotatedString.Builder.appendTopicTag(topicTag: String) {
    withStyle(
        SpanStyle(
            color = Colors.blue_3266FF,
            textDecoration = TextDecoration.Underline,
        ),
    ) {
        append(topicTag)
    }
}

/** 发帖等输入框：与详情正文相同规则高亮内联话题（仅改样式，不改变字符，便于与光标对齐）。 */
internal object PostInlineTopicVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val styled = buildAnnotatedString {
            appendContentWithTopicStyle(text.text)
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}
