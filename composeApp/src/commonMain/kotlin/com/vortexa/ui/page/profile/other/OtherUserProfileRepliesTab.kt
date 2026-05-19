package com.vortexa.ui.page.profile.other

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vortexa.model.UserCenterCommentItem
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.page.post.detail.PostDetailActivity

/**
 * 他人主页「回复」Tab。
 */
@Composable
fun OtherUserProfileRepliesTab(
    comments: List<UserCenterCommentItem>,
    pageStatus: PageStatus,
    hasMore: Boolean,
    loadingMore: Boolean,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onCommentLike: (Long) -> Unit,
    onOpenUserProfile: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            itemsIndexed(
                comments,
                key = { _, item -> item.commentId }
            ) { index, item ->
                OtherUserProfileCommentItem(
                    item = item,
                    onLikeClick = { onCommentLike(item.commentId) },
                    onReplyClick = {
                        PostDetailActivity.startForReplyToComment(
                            context = context,
                            postId = item.postId.toString(),
                            commentId = item.commentId,
                            authorName = item.userName,
                            content = item.content,
                            avatar = item.userAvatar
                        )
                    },
                    onOpenUserProfile = onOpenUserProfile
                )
                if (index == comments.lastIndex && hasMore && !loadingMore) {
                    LaunchedEffect(comments.size, hasMore) {
                        onLoadMore()
                    }
                }
            }
        }
        PageStatusView(
            status = when {
                pageStatus == PageStatus.Success && comments.isEmpty() -> PageStatus.Empty
                else -> pageStatus
            },
            modifier = Modifier.fillMaxSize(),
            emptyMessage = "暂无回复",
            showEmptyRefresh = true,
            onRefresh = onRefresh
        )
    }
}

