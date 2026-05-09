package com.vortexa.ui.page.post.detail.reply

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.collectAsState
import com.vortexa.ui.component.ClickableLinkText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vortexa.config.UserConfig
import com.vortexa.ui.component.AvatarImage
import com.vortexa.ui.component.FollowButton
import com.vortexa.ui.component.FollowButtonSize
import com.vortexa.ui.component.PopupDropdownMenu
import com.vortexa.ui.page.imagepreview.ImagePreviewActivity
import com.vortexa.ui.page.profile.other.OtherUserProfileActivity
import com.vortexa.ui.page.post.detail.PostDetailViewModel
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.appendWithUrlSpans
import com.vortexa.util.extension.click
import com.vortexa.util.formatPostInteractionCount
import vortexa.composeapp.generated.resources.Res

/**
 * 回复项
 * 包含：头像、姓名、「楼主」标签（发帖人）、未关注时的关注按钮、点赞数、回复内容（回复 谁谁谁）、时间
 * @param rootCommentId 所在楼层的一级评论 ID，用于点击「回复」时调用 [PostDetailViewModel.startReplyToReply]
 */
@Composable
fun ReplyItemView(
    reply: Reply,
    rootCommentId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel = viewModel(PostDetailViewModel::class)
    val detailData by viewModel.detailData.collectAsState()
    val followLoading by viewModel.followLoading.collectAsState()
    val unfollowLoading by viewModel.unfollowLoading.collectAsState()
    val currentUserId = UserConfig.getUserId()
    val postAuthorId = detailData?.post?.userId ?: 0L
    val postAuthorFollowed = detailData?.isFollowed == true
    val showLzFollowButton =
        reply.isAuthor &&
            reply.userId != 0L &&
            reply.userId != currentUserId &&
            reply.userId == postAuthorId &&
            !postAuthorFollowed
    var showMoreMenu by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .padding(top = 4.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // 头像：优先使用 reply.avatar（接口 userAvatar），为空时回退默认占位头像
        AvatarImage(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        OtherUserProfileActivity.startIfNotSelf(context, reply.userId)
                    }
                ),
            avatarUrl = (reply.avatar as? String).takeIf { !it.isNullOrBlank() },
            contentDescription = "回复用户头像",
            defaultResId = Res.drawable.profile_default
        )
        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            // 第一行：姓名、点赞
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reply.authorName,
                    style = FontMedium(fontSize = 14, color = Colors.black_101828)
                )
                if (reply.isAuthor) {
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
                    if (showLzFollowButton) {
                        Spacer(modifier = Modifier.width(6.dp))
                        FollowButton(
                            userId = reply.userId,
                            isFollowing = detailData?.isFollowed ?: false,
                            followLoading = followLoading,
                            unfollowLoading = unfollowLoading,
                            onFollowClick = { viewModel.follow(reply.userId) },
                            onUnfollowConfirm = { viewModel.unfollow(reply.userId) },
                            size = FollowButtonSize.Small
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.click {
                        viewModel.onReplyLikeClick(reply)
                    }
                ) {
                    Icon(
                        painter = painterResource(if (reply.isLiked) Res.drawable.heart_small_selected else Res.drawable.heart_small),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (reply.isLiked) Colors.red_FF383C else Colors.gray_6A7282
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = formatPostInteractionCount(reply.likeCount),
                        style = FontRegular(fontSize = 14, color = if (reply.isLiked) Colors.red_FF383C else Colors.gray_6A7282)
                    )
                }
            }

            // 回复内容：Reply 谁谁谁（replyToName 用灰色）+ 正文（含可点击超链接）
            Spacer(modifier = Modifier.height(4.dp))
            val replyAnnotated = buildAnnotatedString {
                withStyle(SpanStyle(color = Colors.black_101828)) {
                    append("回复 ")
                }
                withStyle(SpanStyle(color = Colors.gray_B1B8C6)) {
                    append(reply.replyToName)
                }
                withStyle(SpanStyle(color = Colors.black_101828)) {
                    append(": ")
                }
                appendWithUrlSpans(reply.content, onPlain = { seg ->
                    withStyle(SpanStyle(color = Colors.black_101828)) {
                        append(seg)
                    }
                })
            }
            ClickableLinkText(
                text = replyAnnotated,
                style = FontRegular(fontSize = 12, color = Colors.black_101828)
            )

            // 九宫格图片（文案下方、时间上方）
            if (reply.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                val ctx = LocalContext.current
                CommentReplyMediaGrid(
                    images = reply.images,
                    cellSize = 56.dp,
                    onImageClick = { index, urls -> ImagePreviewActivity.start(ctx, urls, index) }
                )
            }

            // 时间
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier.height(25.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reply.time,
                    style = FontRegular(fontSize = 10, color = Colors.gray_B1B8C6)
                )

                Text(
                    text = "回复",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                        .click {
                            viewModel.startReplyToReply(reply, rootCommentId)
                        },
                    style = FontRegular(12, Colors.gray_6A7282)
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
                            viewModel.startReplyToReply(reply, rootCommentId)
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ReplyItemPreview() {
    ReplyItemView(
        reply = Reply(
            id = "r1",
            authorName = "铅大家将有几个瞬间",
            replyToName = "张三",
            content = "我一般 3-5 倍，不敢开太高",
            likeCount = 5,
            time = "8 分钟前"
        ),
        rootCommentId = "c1"
    )
}
