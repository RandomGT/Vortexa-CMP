package com.vortexa.ui.page.post.detail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.ImageData
import com.mohamedrejeb.richeditor.model.ImageLoader
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import com.vortexa.ui.component.ClickableLinkText
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.resolveApiMediaUrl

/**
 * 使用 Compose Rich Editor 渲染接口返回的 HTML 片段。
 *
 * 该库基于 commonMain 实现，支持 iOS；图片由项目内 Coil3 适配器加载。
 */
@OptIn(ExperimentalRichTextApi::class)
@Composable
fun PostDetailHtmlContent(
    html: String,
    modifier: Modifier = Modifier,
) {
    val richTextState = rememberRichTextState()
    val parseFailed = remember(html) { mutableStateOf(false) }
    val bodyStyle = FontRegular(fontSize = 18, color = Colors.black_101828).copy(lineHeight = 24.sp)

    LaunchedEffect(html) {
        parseFailed.value = false
        runCatching {
            richTextState.setHtml(html)
        }.onFailure {
            parseFailed.value = true
        }
    }

    if (parseFailed.value) {
        ClickableLinkText(
            text = buildPostBodySegmentAnnotatedString(htmlToPlainText(html)),
            style = bodyStyle,
            modifier = modifier.fillMaxWidth(),
        )
    } else {
        RichText(
            state = richTextState,
            modifier = modifier.fillMaxWidth(),
            style = bodyStyle,
            imageLoader = PostHtmlCoilImageLoader,
        )
    }
}

@OptIn(ExperimentalRichTextApi::class)
private object PostHtmlCoilImageLoader : ImageLoader {
    @Composable
    override fun load(model: Any): ImageData? {
        val imageModel = (model as? String)?.let(::resolveApiMediaUrl) ?: model
        val painter = rememberAsyncImagePainter(model = imageModel)
        var imageData by remember { mutableStateOf<ImageData?>(null) }

        LaunchedEffect(painter.state) {
            painter.state.collect { state ->
                imageData = if (state is AsyncImagePainter.State.Success) {
                    ImageData(painter = state.painter)
                } else {
                    null
                }
            }
        }

        return imageData
    }
}

private fun htmlToPlainText(html: String): String {
    return html
        .replace(Regex("(?i)<br\\s*/?>"), "\n")
        .replace(Regex("(?i)</p\\s*>"), "\n")
        .replace(Regex("<[^>]+>"), "")
        .decodeBasicHtmlEntities()
        .trim()
}

private fun String.decodeBasicHtmlEntities(): String =
    replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&apos;", "'")
