package com.vortexa.ui.page.search.result.post

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.page.home.pager.home.HomeCommunicateNavigation
import com.vortexa.ui.page.home.pager.home.recommend.PostItem
import com.vortexa.ui.page.post.detail.PostDetailActivity
import com.vortexa.ui.page.search.result.SearchResultViewModel

/**
 * 搜索结果「帖文」Tab 页面：使用 [SearchResultViewModel] 的帖子列表，
 * 复用 [PostItem] 与列表样式（分割线等）渲染。
 */
@Composable
fun PostPage(
    viewModel: SearchResultViewModel = vortexaViewModel { SearchResultViewModel() },
    modifier: Modifier = Modifier
) {
    val posts by viewModel.postList.collectAsState()
    val pageStatus by viewModel.postListStatus.collectAsState()
    val context = Context()
    val dividerColor = Color(0xFFF3F4F5)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
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
                    onLikeClick = { viewModel.togglePostLike(post.id) },
                    onBookmarkClick = { viewModel.togglePostBookmark(post.id) },
                    onCommentClick = {
                        PostDetailActivity.start(context, post, openReplyComposer = true)
                    },
                    onPostClick = { PostDetailActivity.start(context, post) },
                    onModuleClick = { HomeCommunicateNavigation.startFromPost(context, post) }
                )
            }
        }

        PageStatusView(
            status = pageStatus,
            modifier = Modifier.fillMaxSize(),
            emptyMessage = "暂无搜索结果",
            onRefresh = { viewModel.reloadSearchResult() }
        )
    }
}
