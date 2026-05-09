package com.vortexa.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.vortexa.ui.theme.Colors

/** [AnnotatedString] 中可点击超链接的 annotation tag，用于 [getStringAnnotations]。 */
const val URL_ANNOTATION_TAG = "vortexa_url"

private val URL_REGEX =
    Regex("""(?i)\b(https?://[^\s<>"{}|\\^`\[\]（]+|www\.[^\s<>"{}|\\^`\[\]（]+)""")

private val URL_TRAILING_CHARS: Set<Char> = setOf(
    '.', ',', ';', ':', '!', '?', ')', ']', '}', '"', '\'', '(', '[',
    '。', '，', '、', '：', '；', '！', '？', '」', '』'
)

private val defaultLinkSpanStyle = SpanStyle(
    color = Colors.blue_3266FF,
    textDecoration = TextDecoration.Underline
)

/** 从展示文案中整理出可交给 [Intent.ACTION_VIEW] 的 URL。 */
fun normalizeUrlForIntent(raw: String): String {
    var s = raw.trim()
    while (s.isNotEmpty() && s.last() in URL_TRAILING_CHARS) {
        s = s.dropLast(1).trimEnd()
    }
    return when {
        s.startsWith("http://", ignoreCase = true) || s.startsWith("https://", ignoreCase = true) -> s
        s.startsWith("www.", ignoreCase = true) -> "https://$s"
        else -> s
    }
}

/** 使用系统外部浏览器打开链接。 */
fun Context.openUrlInExternalBrowser(url: String) {
    val normalized = normalizeUrlForIntent(url)
    val uri = try {
        Uri.parse(normalized)
    } catch (_: Exception) {
        return
    }
    if (uri.scheme.isNullOrBlank()) return
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // 无可用浏览器时不提示，避免打扰
    }
}

/**
 * 将 [text] 按 URL 切段：普通片段由 [onPlain] 追加，链接片段带 [URL_ANNOTATION_TAG] 与链接样式。
 */
fun AnnotatedString.Builder.appendWithUrlSpans(
    text: String,
    linkStyle: SpanStyle = defaultLinkSpanStyle,
    onPlain: AnnotatedString.Builder.(String) -> Unit
) {
    if (text.isEmpty()) return
    var idx = 0
    for (match in URL_REGEX.findAll(text)) {
        if (match.range.first > idx) {
            onPlain(text.substring(idx, match.range.first))
        }
        val full = match.value
        val core = full.dropLastWhile { it in URL_TRAILING_CHARS }
        val tail = full.substring(core.length)
        if (core.isNotEmpty()) {
            pushStringAnnotation(tag = URL_ANNOTATION_TAG, annotation = normalizeUrlForIntent(core))
            withStyle(linkStyle) {
                append(core)
            }
            pop()
        }
        if (tail.isNotEmpty()) {
            onPlain(tail)
        }
        idx = match.range.last + 1
    }
    if (idx < text.length) {
        onPlain(text.substring(idx))
    }
}
