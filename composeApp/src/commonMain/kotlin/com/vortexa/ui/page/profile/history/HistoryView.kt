package com.vortexa.ui.page.profile.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import com.vortexa.ui.component.AppLoadingIndicator
import com.vortexa.ui.component.LoadingIndicatorSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.ui.component.ListEndFooter
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.page.home.pager.home.HomeCommunicateNavigation
import com.vortexa.ui.page.home.pager.home.recommend.PostItem
import kotlinx.coroutines.flow.collect
import com.vortexa.ui.page.post.detail.PostDetailActivity
import com.vortexa.ui.page.profile.collection.CollectionFilter
import com.vortexa.ui.theme.belowStatusBar

/**
 * 浏览记录页：头部 + Filter + Post 列表。
 * 样式、Filter 与收藏页一致，使用 [PostItem] 展示记录。
 *
 * @param onBackClick 点击头部返回回调
 */
@Composable
fun HistoryView(
    onBackClick: () -> Unit = {},
    viewModel: HistoryViewModel = vortexaViewModel { HistoryViewModel() },
    modifier: Modifier = Modifier
) {
    val postList by viewModel.postList.collectAsState()
    val pageStatus by viewModel.pageStatus.collectAsState()
    val selectedFilterIndex by viewModel.selectedFilterIndex.collectAsState()
    val hasMoreHistory by viewModel.hasMoreHistory.collectAsState()
    val loadingMoreHistory by viewModel.loadingMoreHistory.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val dividerColor = Color(0xFFF3F4F5)

    LaunchedEffect(listState, hasMoreHistory, loadingMoreHistory, pageStatus, postList.size) {
        snapshotFlow {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = info.totalItemsCount
            lastVisible to total
        }.collect { (lastVisible, total) ->
            if (pageStatus == PageStatus.Success &&
                hasMoreHistory &&
                !loadingMoreHistory &&
                total > 0 &&
                lastVisible >= total - 2
            ) {
                viewModel.loadMoreHistory()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .belowStatusBar()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HistoryHeader(onBackClick = onBackClick)
            CollectionFilter(
                selectedIndex = selectedFilterIndex,
                onChipClick = { viewModel.setFilter(it) }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    itemsIndexed(postList, key = { _, post -> post.id }) { index, post ->
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
                                HomeCommunicateNavigation.startFromPost(context, post)
                            }
                        )
                    }

                    if (pageStatus == PageStatus.Success && postList.isNotEmpty()) {
                        if (loadingMoreHistory) {
                            item(key = "history_load_more") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AppLoadingIndicator(
                                        modifier = Modifier.size(24.dp),
                                        size = LoadingIndicatorSize.Medium,
                                    )
                                }
                            }
                        } else if (!hasMoreHistory) {
                            item(key = "history_list_end") {
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
                    status = when {
                        pageStatus == PageStatus.Success && postList.isEmpty() -> PageStatus.Empty
                        else -> pageStatus
                    },
                    modifier = Modifier.fillMaxSize(),
                    emptyMessage = if (selectedFilterIndex == 0) "暂无浏览记录" else "该分区暂无浏览记录",
                    showEmptyRefresh = true,
                    onRefresh = { viewModel.loadHistory() }
                )
            }
        }
    }
}

@Composable
private fun HistoryViewPreview() {
    HistoryView(onBackClick = {})
}
