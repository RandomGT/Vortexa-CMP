package com.vortexa.ui.page.post.detail.reply

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.config.UserConfig
import com.vortexa.ui.page.post.detail.PostDetailViewModel
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular

/**
 * 完整评论项 = 评论主体 + 回复列表
 */
@Composable
fun CommentItemView(
    comment: Comment,
    viewModel: PostDetailViewModel = vortexaViewModel { PostDetailViewModel() },
    modifier: Modifier = Modifier
) {
    val detailData by viewModel.detailData.collectAsState()
    val followLoading by viewModel.followLoading.collectAsState()
    val unfollowLoading by viewModel.unfollowLoading.collectAsState()
    val currentUserId = UserConfig.getUserId()
    val postAuthorId = detailData?.post?.userId ?: 0L
    val postAuthorFollowed = detailData?.isFollowed == true
    // 仅未关注楼主时在「楼主」后展示关注按钮（已关注则与标题栏状态一致，不再重复）
    val showLzFollowButton =
        comment.isAuthor &&
            comment.userId != 0L &&
            comment.userId != currentUserId &&
            comment.userId == postAuthorId &&
            !postAuthorFollowed

    Column(modifier = modifier.fillMaxWidth()) {
        CommentBody(
            comment = comment,
            onLikeClick = { viewModel.onLikeClick(comment) },
            onReplyClick = { viewModel.startReplyToComment(comment) },
            showLzFollowButton = showLzFollowButton,
            lzFollowUserId = comment.userId,
            lzIsFollowed = detailData?.isFollowed ?: false,
            followLoading = followLoading,
            unfollowLoading = unfollowLoading,
            onLzFollowClick = { viewModel.follow(comment.userId) },
            onLzUnfollowConfirm = { viewModel.unfollow(comment.userId) }
        )
        if (comment.replies.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(start = 44.dp, top = 10.dp)
                    .background(Colors.gray_F8F9FA, RoundedCornerShape(4.dp))
                    .padding(10.dp)
            ) {
                // 回复列表
                comment.replies.forEach { reply ->
                    ReplyItemView(
                        reply = reply,
                        rootCommentId = comment.id,
                        viewModel = viewModel
                    )
                }
                if (comment.hasMoreReplies) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (comment.repliesLoadingMore) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Colors.blue_3266FF
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "更多回复",
                            style = FontRegular(fontSize = 13, color = Colors.blue_3266FF),
                            modifier = Modifier.clickable(enabled = !comment.repliesLoadingMore) {
                                viewModel.loadMoreReplies(comment.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
