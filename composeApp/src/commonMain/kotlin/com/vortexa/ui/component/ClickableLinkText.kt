package com.vortexa.ui.component

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import com.vortexa.ui.theme.BaseTheme
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.URL_ANNOTATION_TAG
import com.vortexa.util.appendWithUrlSpans
import com.vortexa.util.openUrlInExternalBrowser

/**
 * 可识别 [AnnotatedString] 中带 [URL_ANNOTATION_TAG] 的区间，点击后在系统浏览器中打开。
 */
@Composable
fun ClickableLinkText(
    text: AnnotatedString,
    style: TextStyle,
    modifier: Modifier = Modifier,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
) {
    val context: Any? = null
    ClickableText(
        text = text,
        modifier = modifier,
        style = style,
        softWrap = softWrap,
        overflow = overflow,
        maxLines = maxLines,
        onClick = { offset ->
            text.getStringAnnotations(
                tag = URL_ANNOTATION_TAG,
                start = offset,
                end = offset + 1
            ).firstOrNull()?.let { context.openUrlInExternalBrowser(it.item) }
        }
    )
}

@Composable
private fun ClickableLinkTextPreview() {
    BaseTheme {
        val annotated = buildAnnotatedString {
            appendWithUrlSpans("官网 https://example.com 欢迎访问") { append(it) }
        }
        ClickableLinkText(
            text = annotated,
            style = FontRegular(fontSize = 16, color = Colors.black_101828)
        )
    }
}
