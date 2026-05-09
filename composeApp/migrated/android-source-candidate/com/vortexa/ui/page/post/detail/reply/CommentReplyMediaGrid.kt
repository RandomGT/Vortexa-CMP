package com.vortexa.ui.page.post.detail.reply

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vortexa.lib_net.client.RetrofitClient
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.toImagePreviewUrls
import vortexa.composeapp.generated.resources.Res

/**
 * 评论/回复九宫格媒体展示（最多 9 张，每行 3 张）。
 * 展示在文案下方、时间上方。
 *
 * @param images 媒体列表，支持 String(URL)、Uri、Int(资源ID)
 * @param cellSize 单格尺寸，默认 64.dp
 * @param onImageClick 点击某张图片时回调 (index, urls)；null 时不响应点击
 */
@Composable
fun CommentReplyMediaGrid(
    images: List<Any>,
    modifier: Modifier = Modifier,
    cellSize: Dp = 64.dp,
    onImageClick: ((index: Int, urls: List<String>) -> Unit)? = null
) {
    val context = LocalContext.current
    val displayImages = images.take(9)
    if (displayImages.isEmpty()) return
    val previewUrls = remember(images, context) { toImagePreviewUrls(images, context) }
    val rows = displayImages.chunked(3)
    val placeholderPainter = painterResource(Res.drawable.default_pic)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        var flatIndex = 0
        rows.forEach { rowImages ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                rowImages.forEach { item ->
                    val currentIndex = flatIndex
                    val clickableModifier = if (onImageClick != null && previewUrls.isNotEmpty()) {
                        Modifier.clickable(
                            interactionSource = MutableInteractionSource(),
                            indication = null,
                            onClick = {
                                val idx = currentIndex.coerceIn(0, previewUrls.size - 1)
                                onImageClick(idx, previewUrls)
                            }
                        )
                    } else Modifier
                    Box(
                        modifier = clickableModifier
                            .size(cellSize)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Colors.gray_F3F5F7)
                    ) {
                        when (item) {
                            is Uri -> {
                                AsyncImage(
                                    model = item,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop,
                                    placeholder = placeholderPainter,
                                    error = placeholderPainter,
                                    fallback = placeholderPainter
                                )
                            }
                            is String -> {
                                val model = resolveCommentMediaUrl(item)
                                if (model != null) {
                                    AsyncImage(
                                        model = model,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop,
                                        placeholder = placeholderPainter,
                                        error = placeholderPainter,
                                        fallback = placeholderPainter
                                    )
                                } else {
                                    Text(
                                        text = "IMG",
                                        style = FontRegular(fontSize = 10, color = Colors.gray_B1B8C6),
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                            is Int -> {
                                Image(
                                    painter = painterResource(item),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                Text(
                                    text = "IMG",
                                    style = FontRegular(fontSize = 10, color = Colors.gray_B1B8C6),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                    flatIndex++
                }
            }
        }
    }
}

/**
 * 解析评论/回复媒体 URL，兼容绝对路径与相对路径。
 */
private fun resolveCommentMediaUrl(rawUrl: String): String? {
    val trimmed = rawUrl.trim()
    if (trimmed.isBlank()) return null
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
    val baseUrl = RetrofitClient.getConfig()?.baseUrl?.trimEnd('/').orEmpty()
    if (baseUrl.isBlank()) {
        Log.w("CommentReplyMediaGrid", "resolveCommentMediaUrl: baseUrl empty")
        return trimmed
    }
    return "$baseUrl/${trimmed.trimStart('/')}"
}

@Preview
@Composable
private fun CommentReplyMediaGridPreview() {
    CommentReplyMediaGrid(
        images = listOf("https://example.com/1.jpg", "https://example.com/2.jpg")
    )
}
