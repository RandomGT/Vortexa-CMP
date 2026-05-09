package com.vortexa.ui.page.home.pager.home.recommend

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.DrawableResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vortexa.model.Post
import com.vortexa.ui.page.imagepreview.ImagePreviewActivity
import com.vortexa.ui.component.AvatarImage
import com.vortexa.ui.page.profile.other.OtherUserProfileActivity
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontBold
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.formatPostInteractionCount
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.bookmark_line
import vortexa.composeapp.generated.resources.bookmark_selected
import vortexa.composeapp.generated.resources.default_pic
import vortexa.composeapp.generated.resources.heart_small
import vortexa.composeapp.generated.resources.heart_small_selected
import vortexa.composeapp.generated.resources.message_circle
import vortexa.composeapp.generated.resources.profile_default

/**
 * 单条帖子 Item（Figma 747-81592）
 * 包含头像、用户名、时间、标签、内容、图片网格、互动按钮。
 *
 * @param onPostClick 点击帖子进入详情，null 则不响应
 * @param onModuleClick 点击右上角板块标签，null 则不单独响应（避免与 [onPostClick] 冲突时子级优先）
 */
@Composable
fun PostItem(
    post: Post,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onCommentClick: () -> Unit,
    onPostClick: (() -> Unit)? = null,
    onModuleClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = Context()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .then(
                if (onPostClick != null) Modifier.clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null,
                    onClick = onPostClick
                ) else Modifier
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val viewTimeText = post.viewTime?.trim()?.takeIf { it.isNotEmpty() }
            if (viewTimeText != null) {
                Text(
                    text = viewTimeText,
                    style = FontRegular(fontSize = 12, color = Colors.gray_B1B8C6),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            // Header：头像、首行作者名（占满剩余空间）+ 板块 #xxx（接口字段 module，Figma 222-10267）
            val moduleRaw = post.module?.trim()?.takeIf { it.isNotEmpty() }
                ?: post.tagName?.trim()?.takeIf { it.isNotEmpty() }
            val moduleLabel = moduleRaw?.let { raw ->
                if (raw.startsWith("#")) raw else "#$raw"
            }
            val moduleTagColor = Colors.blue_277DFF.copy(alpha = 0.64f)

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarImage(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                OtherUserProfileActivity.startIfNotSelf(context, post.userId)
                            }
                        ),
                    avatarUrl = (post.avatar as? String).takeIf { !it.isNullOrBlank() },
                    contentDescription = "用户头像",
                    defaultResId = Res.drawable.profile_default
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = post.username,
                            style = FontMedium(fontSize = 16, color = Colors.black_101828),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        OtherUserProfileActivity.startIfNotSelf(context, post.userId)
                                    }
                                )
                        )
                        if (moduleLabel != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = moduleLabel,
                                style = FontMedium(
                                    fontSize = 12,
                                    color = moduleTagColor
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.then(
                                    if (onModuleClick != null) {
                                        Modifier.clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = onModuleClick
                                        )
                                    } else Modifier
                                )
                            )
                        }
                    }
                    Text(
                        text = post.time,
                        style = FontRegular(fontSize = 12, color = Colors.gray_B1B8C6)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = post.title ?: "",
                style = FontBold(fontSize = 16, color = Colors.black_101828),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                modifier= Modifier.padding(top = 8.dp),
                text = post.content,
                style = FontRegular(fontSize = 14, color = Colors.black_101828),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (post.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                val ctx = LocalContext.current
                PostImagesGrid(
                    images = post.images,
                    onImageClick = { index, urls -> ImagePreviewActivity.start(ctx, urls, index) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 收藏后若接口返回前的数量为 0，先按 1 展示，避免文案继续显示 Save。
            val bookmarkText = when {
                post.collectCount > 0 -> formatPostInteractionCount(post.collectCount)
                post.isCollect -> "1"
                else -> "Save"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InteractionButton(
                    icon = if (post.isCollect) Res.drawable.bookmark_selected else Res.drawable.bookmark_line,
                    text = bookmarkText,
                    isActive = post.isCollect,
                    onClick = onBookmarkClick
                )
                Spacer(modifier = Modifier.width(16.dp))
                InteractionButton(
                    icon = if (post.isLiked) Res.drawable.heart_small_selected else Res.drawable.heart_small,
                    text = if (post.likeCount > 0) formatPostInteractionCount(post.likeCount) else "Like",
                    isActive = post.isLiked,
                    activeColor = Colors.red_FF383C,
                    onClick = onLikeClick
                )
                Spacer(modifier = Modifier.width(16.dp))
                InteractionButton(
                    icon = Res.drawable.message_circle,
                    text = if (post.commentCount > 0) formatPostInteractionCount(post.commentCount) else "Comment",
                    isActive = false,
                    onClick = onCommentClick
                )
            }
        }
    }
}

/**
 * 帖子图片网格（最多 9 张，每行 3 张）。
 * 每格固定为正方形（宽高一致），支持网络 URL（String）、Uri、本地资源 ID（Int）。
 *
 * @param images 图片列表
 * @param onImageClick 点击某张图片时回调 (index, urls)，index 为点击的索引，urls 为完整列表；null 时不响应点击
 */
@Composable
fun PostImagesGrid(
    images: List<Any>,
    onImageClick: ((index: Int, urls: List<String>) -> Unit)? = null
) {
    val context = Context()
    val displayImages = images.take(9)
    val rows = displayImages.chunked(3)
    val placeholderPainter = painterResource(Res.drawable.default_pic)
    val imageSize = 104.dp
    val previewUrls = remember(images, context) {
        com.vortexa.util.toImagePreviewUrls(images, context)
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                            .size(imageSize)
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
                                val model = resolvePostImageModel(item)
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
                                }
                            }
                            else -> {
                                // 未知类型保留占位
                            }
                        }
                    }
                    flatIndex++
                }
                repeat(3 - rowImages.size) {
                    Spacer(modifier = Modifier.size(imageSize))
                }
            }
        }
    }
}

/**
 * 解析帖子图片模型，统一处理空值与相对路径。
 * @param rawUrl 接口原始图片地址，可能是绝对地址或相对地址。
 * @return 可供 Coil 加载的最终地址；无效时返回 null。
 */
private fun resolvePostImageModel(rawUrl: String): String? {
    val trimmed = rawUrl.trim()
    if (trimmed.isBlank()) return null
    return trimmed
}

/**
 * 互动按钮（收藏、点赞、评论）
 */
@Composable
fun InteractionButton(
    icon: DrawableResource,
    text: String,
    isActive: Boolean,
    activeColor: Color = Colors.gold_F6BD49,
    defaultColor: Color = Colors.gray_6A7282,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (isActive) activeColor else defaultColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = FontRegular(fontSize = 12, color = Colors.gray_6A7282)
        )
    }
}

@Composable
fun PostItemPreview() {
    val post = Post(
        id = "1",
        username = "Alex",
        time = "10 min ago",
        content = "Exploring design trends for 2026.",
        images = emptyList(),
        tagName = "Design",
        likeCount = 12,
        commentCount = 3,
        isLiked = true
    )
    PostItem(
        post = post,
        onLikeClick = {},
        onBookmarkClick = {},
        onCommentClick = {}
    )
}
