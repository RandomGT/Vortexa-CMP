package com.vortexa.ui.page.post.detail.reply

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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.component.AvatarImage
import com.vortexa.ui.component.ClickableLinkText
import com.vortexa.ui.component.FollowButton
import com.vortexa.ui.component.FollowButtonSize
import com.vortexa.ui.component.PopupDropdownMenu
import com.vortexa.ui.page.imagepreview.ImagePreviewActivity
import com.vortexa.ui.page.profile.other.OtherUserProfileActivity
import com.vortexa.ui.theme.BaseTheme
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.formatPostInteractionCount
import com.vortexa.util.appendWithUrlSpans
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res

/**
 * 评论主体
 * 包含：头像、姓名、「楼主」标签（发帖人）、关注按钮、点赞数、内容、图片、时间
 * @param showLzFollowButton 楼主、非本人且当前用户未关注发帖人时，在「楼主」标签后展示关注按钮
 * @param lzFollowUserId inline 关注的目标用户 ID（应与发帖人一致）
 * @param lzIsFollowed 是否已关注发帖人（与 [PostDetailTitleBar] 同源）
 */
@Composable
fun CommentBody(
    comment: Comment,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    showLzFollowButton: Boolean = false,
    lzFollowUserId: Long = 0L,
    lzIsFollowed: Boolean = false,
    followLoading: Boolean = false,
    unfollowLoading: Boolean = false,
    onLzFollowClick: () -> Unit = {},
    onLzUnfollowConfirm: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMoreMenu by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        // 第一行：头像、姓名、Tag、关注、点赞
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像：优先使用 comment.avatar（接口 userAvatar），为空时回退默认占位头像
            AvatarImage(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            OtherUserProfileActivity.startIfNotSelf(context, comment.userId)
                        }
                    ),
                avatarUrl = (comment.avatar as? String).takeIf { !it.isNullOrBlank() },
                contentDescription = "评论用户头像",
                defaultResId = Res.drawable.profile_default
            )
            Spacer(modifier = Modifier.width(8.dp))

            // 姓名
            Text(
                text = comment.authorName,
                style = FontMedium(fontSize = 14, color = Colors.black_101828)
            )

            // Tag：楼主，发帖人评论时始终显示
            if (comment.isAuthor) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = Colors.blue_3266FF.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "楼主",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = FontRegular(fontSize = 10, color = Colors.blue_3266FF)
                    )
                }
                if (showLzFollowButton && lzFollowUserId != 0L) {
                    Spacer(modifier = Modifier.width(6.dp))
                    FollowButton(
                        userId = lzFollowUserId,
                        isFollowing = lzIsFollowed,
                        followLoading = followLoading,
                        unfollowLoading = unfollowLoading,
                        onFollowClick = onLzFollowClick,
                        onUnfollowConfirm = onLzUnfollowConfirm,
                        size = FollowButtonSize.Small
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))


            // 点赞
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onLikeClick)
            ) {
                Icon(
                    painter = painterResource(if (comment.isLiked) Res.drawable.heart_small_selected else Res.drawable.heart_small),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (comment.isLiked) Colors.red_FF383C else Colors.gray_6A7282
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatPostInteractionCount(comment.likeCount),
                    style = FontRegular(fontSize = 14, color = if (comment.isLiked) Colors.red_FF383C else Colors.gray_6A7282)
                )
            }
        }

        // 内容
        Spacer(modifier = Modifier.height(8.dp))
        val commentAnnotated = buildAnnotatedString {
            appendWithUrlSpans(comment.content, onPlain = { append(it) })
        }
        ClickableLinkText(
            text = commentAnnotated,
            style = FontRegular(fontSize = 16, color = Colors.black_101828),
            modifier = Modifier.padding(start = 44.dp) // 与头像对齐
        )

        // 九宫格图片（文案下方、时间上方）
        if (comment.images.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            val ctx = LocalContext.current
            CommentReplyMediaGrid(
                images = comment.images,
                modifier = Modifier.padding(start = 44.dp),
                cellSize = 72.dp,
                onImageClick = { index, urls -> ImagePreviewActivity.start(ctx, urls, index) }
            )
        }

        // 时间
        Spacer(modifier = Modifier.height(8.dp))

        Row() {
            Text(
                text = comment.time,
                style = FontRegular(fontSize = 12, color = Colors.gray_B1B8C6),
                modifier = Modifier.padding(start = 44.dp)
                    .weight(1f)
            )

            Box {
                Image(
                    painterResource(Res.drawable.icon_more),
                    modifier = Modifier.click { showMoreMenu = true },
                    contentDescription = "",
                )
                PopupDropdownMenu(
                    modifier = Modifier.width(72.dp),
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false },
                    options = listOf("回复"),
                    onOptionClick = { _ ->
                        showMoreMenu = false
                        onReplyClick()
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CommentBodyPreview() {
    BaseTheme {
        CommentBody(
            comment = Comment(
                id = "c1",
                authorName = "张三",
                userId = 10001L,
                isAuthor = true,
                content = "感谢分享！最近也在关注合约",
                likeCount = 12,
                time = "10 分钟前"
            ),
            onLikeClick = {},
            onReplyClick = {},
            showLzFollowButton = true,
            lzFollowUserId = 10001L,
            lzIsFollowed = false
        )
    }
}
