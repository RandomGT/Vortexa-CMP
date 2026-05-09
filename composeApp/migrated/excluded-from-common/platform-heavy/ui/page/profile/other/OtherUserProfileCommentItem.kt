package com.vortexa.ui.page.profile.other

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import com.vortexa.lib_net.client.RetrofitClient
import com.vortexa.model.UserCenterCommentItem
import com.vortexa.ui.component.AvatarImage
import com.vortexa.ui.component.PopupDropdownMenu
import com.vortexa.ui.page.home.pager.home.recommend.PostImagesGrid
import com.vortexa.ui.page.imagepreview.ImagePreviewActivity
import com.vortexa.ui.theme.BaseTheme
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click
import com.vortexa.util.formatPostInteractionCount
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.heart_small
import vortexa.composeapp.generated.resources.heart_small_selected
import vortexa.composeapp.generated.resources.icon_more
import vortexa.composeapp.generated.resources.profile_default

/**
 * 他人主页「回复」Tab 单条样式（Figma 415-41416）。
 */
@Composable
fun OtherUserProfileCommentItem(
    item: UserCenterCommentItem,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context: Any? = null
    var showMoreMenu by remember { mutableStateOf(false) }
    val avatarUrl = remember(item.userAvatar) { resolveUserCenterMediaUrl(item.userAvatar) }
    val onReviewerProfileClick = {
        OtherUserProfileActivity.startIfNotSelf(context, item.userId)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 6.dp)
            .padding(horizontal = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            AvatarImage(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onReviewerProfileClick
                    ),
                avatarUrl = avatarUrl,
                contentDescription = "头像",
                defaultResId = Res.drawable.profile_default
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onReviewerProfileClick
                            )
                    ) {
                        Text(
                            text = item.userName,
                            style = FontRegular(fontSize = 14, color = Colors.gray_6A7282)
                        )
                        if (item.isAuthor) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                color = Color(0xFFE4EFFF),
                                shape = RoundedCornerShape(2.dp)
                            ) {
                                Text(
                                    text = "楼主",
                                    style = FontRegular(fontSize = 9, color = Colors.blue_277DFF),
                                    modifier = Modifier.padding(horizontal = 3.dp)
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.click { onLikeClick() }
                    ) {
                        Icon(
                            painter = painterResource(
                                if (item.isLiked) Res.drawable.heart_small_selected else Res.drawable.heart_small
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (item.isLiked) Colors.red_FF383C else Colors.gray_6A7282
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatPostInteractionCount(item.likeCount),
                            style = FontRegular(
                                fontSize = 14,
                                color = if (item.isLiked) Colors.red_FF383C else Colors.gray_6A7282
                            )
                        )
                    }
                }
                Text(
                    text = item.content,
                    style = FontRegular(fontSize = 16, color = Colors.black_101828),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
                val commentImages = remember(item.mediaList) {
                    item.mediaList.orEmpty().filter { it.isNotBlank() }.map { it as Any }
                }
                if (commentImages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    PostImagesGrid(
                        images = commentImages,
                        onImageClick = { index, urls ->
                            ImagePreviewActivity.start(context, urls, index)
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatUserCenterReplyTime(item.publishTime),
                        style = FontRegular(fontSize = 12, color = Colors.gray_B1B8C6)
                    )
                    Text(
                        text = "回复",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                            .click { onReplyClick() },
                        style = FontRegular(fontSize = 12, color = Colors.gray_6A7282)
                    )
                    Box {
                        Image(
                            painter = painterResource(Res.drawable.icon_more),
                            contentDescription = "更多",
                            modifier = Modifier.click { showMoreMenu = true }
                        )
                        PopupDropdownMenu(
                            modifier = Modifier.width(72.dp),
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            options = listOf("回复"),
                            onOptionClick = { index ->
                                showMoreMenu = false
                                if (index == 0) onReplyClick()
                            }
                        )
                    }
                }
            }
        }
    }
}

internal fun resolveUserCenterMediaUrl(raw: String?): String? {
    val trimmed = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
    val baseUrl = RetrofitClient.getConfig()?.baseUrl?.trimEnd('/').orEmpty()
    if (baseUrl.isBlank()) return trimmed
    return "$baseUrl/${trimmed.trimStart('/')}"
}

internal fun formatUserCenterReplyTime(raw: String): String {
    val parts = raw.trim().split(" ", limit = 2)
    if (parts.size < 2) return raw
    val datePart = parts[0].split("-")
    val timePart = parts[1].split(":")
    if (datePart.size < 3 || timePart.size < 2) return raw
    val month = datePart[1]
    val day = datePart[2]
    val hour = timePart[0]
    val minute = timePart[1].take(2)
    return "$month/$day $hour:$minute"
}

@Composable
private fun OtherUserProfileCommentItemPreview() {
    BaseTheme {
        OtherUserProfileCommentItem(
            item = UserCenterCommentItem(
                commentId = 1,
                postId = 1,
                userId = 1,
                userAvatar = null,
                userName = "Marina",
                content = "价值投资是一种长期投资策略。",
                likeCount = 34,
                publishTime = "2025-12-22 22:30:00",
                isAuthor = true,
                isLiked = false,
                mediaList = emptyList()
            ),
            onLikeClick = {},
            onReplyClick = {}
        )
    }
}
