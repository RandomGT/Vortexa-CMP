package com.vortexa.ui.page.post.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vortexa.ui.component.ClickableLinkText
import com.vortexa.ui.page.imagepreview.ImagePreviewActivity
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontBold
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.resolveApiMediaUrl
import com.vortexa.util.openUrlInExternalBrowser
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.default_pic

/**
 * 按接口 blocks 顺序纵向渲染：文本（含链接/话题）、图片、视频（封面 + 点击打开链接）。
 */
@Composable
fun PostDetailRichContent(
    blocks: List<PostContentBlock>,
    modifier: Modifier = Modifier,
) {
    val context: Any? = null
    val placeholder = painterResource(Res.drawable.default_pic)
    val defaultBodyStyle = FontRegular(fontSize = 18, color = Colors.black_101828)
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        blocks.forEachIndexed { index, block ->
            key(index) {
                when (block) {
                    is PostContentBlock.Text -> {
                        if (block.content.isNotBlank()) {
                            val textStyle = textStyleForBlock(block.style, defaultBodyStyle)
                            ClickableLinkText(
                                text = buildPostBodySegmentAnnotatedString(block.content),
                                style = textStyle,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (index > 0) Modifier.padding(top = 8.dp) else Modifier,
                                    ),
                            )
                        }
                    }
                    is PostContentBlock.Image -> {
                        val imageUrl = resolveApiMediaUrl(block.url)
                        val ratio = if (block.width != null && block.height != null && block.height!! > 0) {
                            block.width!!.toFloat() / block.height!!.toFloat()
                        } else null
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "正文图片",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Colors.gray_F3F5F7)
                                .clickable(
                                    interactionSource = MutableInteractionSource(),
                                    indication = null,
                                ) {
                                    ImagePreviewActivity.start(context, listOfNotNull(imageUrl), 0)
                                }
                                .then(
                                    if (ratio != null) Modifier.aspectRatio(ratio)
                                    else Modifier.heightIn(min = 120.dp, max = 360.dp),
                                ),
                            contentScale = ContentScale.Fit,
                            placeholder = placeholder,
                            error = placeholder,
                            fallback = placeholder,
                        )
                    }
                    is PostContentBlock.Video -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Colors.gray_F3F5F7)
                                .clickable(
                                    interactionSource = MutableInteractionSource(),
                                    indication = null,
                                ) { context.openUrlInExternalBrowser(block.url) },
                            contentAlignment = Alignment.Center,
                        ) {
                            val coverUrl = resolveApiMediaUrl(block.cover)
                            if (coverUrl != null) {
                                AsyncImage(
                                    model = coverUrl,
                                    contentDescription = "视频封面",
                                    modifier = Modifier.matchParentSize(),
                                    contentScale = ContentScale.Crop,
                                    placeholder = placeholder,
                                    error = placeholder,
                                    fallback = placeholder,
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(Colors.gray_F3F5F7),
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Black.copy(alpha = 0.25f)),
                            )
                            Icon(
                                imageVector = Icons.Filled.PlayCircle,
                                contentDescription = "播放",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun textStyleForBlock(
    style: PostContentTextStyle,
    default: TextStyle,
): TextStyle {
    val size = style.fontSizeSp ?: (default.fontSize.value.toInt().takeIf { it > 0 } ?: 18)
    val color = Colors.black_101828
    return when {
        style.bold -> FontBold(fontSize = size, color = color)
        style.fontSizeSp != null -> FontRegular(fontSize = size, color = color)
        else -> default
    }
}

@Composable
private fun PostDetailRichContentPreview() {
    val json = """
        {"blocks":[
          {"type":"text","content":"段落一 https://baidu.com","style":{"bold":true,"fontSize":16}},
          {"type":"image","url":"https://picsum.photos/300/200","width":300,"height":200},
          {"type":"video","url":"https://example.com/a.mp4","cover":"https://picsum.photos/320/180"},
          {"type":"text","content":"说明文字"}
        ]}
    """.trimIndent()
    val blocks = parsePostContentBlocksOrNull(json).orEmpty()
    PostDetailRichContent(blocks = blocks)
}
