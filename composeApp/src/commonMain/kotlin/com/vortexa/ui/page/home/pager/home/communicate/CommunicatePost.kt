package com.vortexa.ui.page.home.pager.home.communicate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.ui.component.DefaultColorLabel
import com.vortexa.ui.component.ListEndFooter
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.page.home.pager.home.recommend.PostItem
import com.vortexa.ui.page.post.detail.PostDetailActivity
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.collect

/**
 * 交流页帖子列表区：仅在整体列表层面将「首帖 | 导师推荐 | 其余帖子」排版（等同仅对第一页数据的视觉切割/中间穿插）；
 * 后续分页追加的帖子只出现在导师区下方。接近底部自动加载下一页；无更多时展示 [ListEndFooter]。
 */
@Composable
fun CommunicatePost(
    scrollToTopWhenActive: Boolean = true,
    viewModel: CommunicateViewModel = vortexaViewModel { CommunicateViewModel() }
) {
    val selectedPostType by viewModel.selectedPostType.collectAsState()
    val postList by viewModel.postList.collectAsState()
    val pageStatus by viewModel.pageStatus.collectAsState()
    val tutorRecommendList by viewModel.tutorRecommendList.collectAsState()
    val hasMorePosts by viewModel.hasMorePosts.collectAsState()
    val loadingMorePosts by viewModel.loadingMorePosts.collectAsState()
    val context: Any? = null
    val dividerColor = Color(0xFFF3F4F5)

    val listState = rememberLazyListState()

    // Pager 预组合可能导致首次切入偏移：仅当用户尚未滚离过顶部时才自动 scrollToTop；滚过后保留位置
    LaunchedEffect(scrollToTopWhenActive) {
        if (!scrollToTopWhenActive) return@LaunchedEffect
        if (!viewModel.communicateListUserScrolled) {
            awaitFrame()
            listState.scrollToItem(0, 0)
        }
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            if (index != 0 || offset != 0) {
                viewModel.markCommunicateListUserScrolled()
            }
        }
    }

    // 接近底部时加载下一页（与评论列表一致：预留 2 个 item 触发）
    LaunchedEffect(listState, hasMorePosts, loadingMorePosts, pageStatus) {
        snapshotFlow {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = info.totalItemsCount
            lastVisible to total
        }.collect { (lastVisible, total) ->
            if (pageStatus == PageStatus.Success &&
                hasMorePosts &&
                !loadingMorePosts &&
                total > 0 &&
                lastVisible >= total - 2
            ) {
                viewModel.loadMorePosts()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (postList.isNotEmpty()) {
                item(key = "post_${postList.first().id}") {
                    PostItem(
                        post = postList.first(),
                        onLikeClick = { viewModel.toggleLike(postList.first().id) },
                        onBookmarkClick = { viewModel.toggleBookmark(postList.first().id) },
                        onCommentClick = {
                            PostDetailActivity.start(context, postList.first(), openReplyComposer = true)
                        },
                        onPostClick = { PostDetailActivity.start(context, postList.first()) },
                        onModuleClick = {
                            val p = postList.first()
                            val raw = p.module?.trim()?.takeIf { it.isNotEmpty() }
                                ?: p.tagName?.trim()?.takeIf { it.isNotEmpty() }
                            if (raw != null) viewModel.loadPosts(moduleLabelToCommunicatePostType(raw))
                        }
                    )
                }
            }

            item(key = "tutor_recommend_block") {
                Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                    DefaultColorLabel("导师", "推荐", showMore = false)
                    Spacer(modifier = Modifier.height(12.dp))
                    TutorRecommendHorizontal(items = tutorRecommendList.take(6))
                }
            }

            itemsIndexed(
                items = postList.drop(1),
                key = { _, post -> post.id }
            ) { index, post ->
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
                    onLikeClick = { viewModel.toggleLike(post.id) },
                    onBookmarkClick = { viewModel.toggleBookmark(post.id) },
                    onCommentClick = {
                        PostDetailActivity.start(context, post, openReplyComposer = true)
                    },
                    onPostClick = { PostDetailActivity.start(context, post) },
                    onModuleClick = {
                        val raw = post.module?.trim()?.takeIf { it.isNotEmpty() }
                            ?: post.tagName?.trim()?.takeIf { it.isNotEmpty() }
                        if (raw != null) viewModel.loadPosts(moduleLabelToCommunicatePostType(raw))
                    }
                )
            }

            if (pageStatus == PageStatus.Success && postList.isNotEmpty()) {
                if (loadingMorePosts) {
                    item(key = "communicate_load_more") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                } else if (!hasMorePosts) {
                    item(key = "communicate_list_end") {
                        ListEndFooter(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    }
                }
            }
        }
        PageStatusView(
            status = pageStatus,
            modifier = Modifier.fillMaxSize(),
            onRefresh = { viewModel.loadPosts(selectedPostType) }
        )
    }
}

@Composable
fun CommunicatePostPreview() {
    Box(Modifier.background(Color.White)) {
        CommunicatePost()
    }
}
