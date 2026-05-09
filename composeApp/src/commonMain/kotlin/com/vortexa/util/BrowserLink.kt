package com.vortexa.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.vortexa.platform.ExternalBrowser
import com.vortexa.ui.theme.Colors

const val URL_ANNOTATION_TAG: String = "vortexa_url"

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

fun AnnotatedString.Builder.appendWithUrlSpans(
    text: String,
    linkStyle: SpanStyle = defaultLinkSpanStyle,
    onPlain: AnnotatedString.Builder.(String) -> Unit,
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
            withStyle(linkStyle) { append(core) }
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

fun openUrlInExternalBrowser(context: Any?, url: String) {
    ExternalBrowser.open(normalizeUrlForIntent(url))
}

fun Any?.openUrlInExternalBrowser(url: String) {
    openUrlInExternalBrowser(this, url)
}
