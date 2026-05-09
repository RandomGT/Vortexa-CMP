package com.vortexa.ui.page.profile.other

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.model.Post
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.page.home.pager.home.HomeCommunicateNavigation
import com.vortexa.ui.page.home.pager.home.recommend.PostItem
import com.vortexa.ui.page.post.detail.PostDetailActivity
import com.vortexa.ui.theme.BaseTheme

/**
 * 他人主页「发帖」Tab：[PostItem] 列表 + 分页加载。
 */
@Composable
fun OtherUserProfilePostsTab(
    posts: List<Post>,
    pageStatus: PageStatus,
    hasMore: Boolean,
    loadingMore: Boolean,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onLikeClick: (String) -> Unit,
    onBookmarkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dividerColor = Color(0xFFF3F4F5)

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            itemsIndexed(posts, key = { _, post -> post.id }) { index, post ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        thickness = 1.dp,
                        color = dividerColor
                    )
                }
                PostItem(
                    post = post,
                    onLikeClick = { onLikeClick(post.id) },
                    onBookmarkClick = { onBookmarkClick(post.id) },
                    onCommentClick = {
                        PostDetailActivity.start(context, post, openReplyComposer = true)
                    },
                    onPostClick = { PostDetailActivity.start(context, post) },
                    onModuleClick = { HomeCommunicateNavigation.startFromPost(context, post) }
                )
                if (index == posts.lastIndex && hasMore && !loadingMore) {
                    LaunchedEffect(posts.size, hasMore) {
                        onLoadMore()
                    }
                }
            }
        }
        PageStatusView(
            status = when {
                pageStatus == PageStatus.Success && posts.isEmpty() -> PageStatus.Empty
                else -> pageStatus
            },
            modifier = Modifier.fillMaxSize(),
            emptyMessage = "暂无发帖",
            showEmptyRefresh = true,
            onRefresh = onRefresh
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun OtherUserProfilePostsTabPreview() {
    BaseTheme {
        OtherUserProfilePostsTab(
            posts = emptyList(),
            pageStatus = PageStatus.Empty,
            hasMore = false,
            loadingMore = false,
            onLoadMore = {},
            onRefresh = {},
            onLikeClick = {},
            onBookmarkClick = {}
        )
    }
}
