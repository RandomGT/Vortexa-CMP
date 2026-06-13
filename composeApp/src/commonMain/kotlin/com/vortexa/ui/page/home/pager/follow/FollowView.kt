package com.vortexa.ui.page.home.pager.follow

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.ui.component.ListEndFooter
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.page.home.pager.home.HomeCommunicateNavigation
import com.vortexa.ui.page.home.pager.home.recommend.PostItem
import com.vortexa.ui.page.post.detail.PostDetailActivity
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.belowStatusBar
import com.vortexa.ui.theme.FontMedium
import com.vortexa.config.TokenConfig
import kotlinx.coroutines.flow.collect

/**
 * 关注页（Figma 747-85095）
 * 顶部标题「关注」+ 横向已关注用户 + 帖子列表（复用 PostItem）；列表接近底部自动加载下一页。
 * 关注用户接口成功后若列表为空，整页展示 [PageStatus.Empty]（与 [FollowedUserHorizontal] 无数据一致）。
 * 已登录：[androidx.lifecycle.Lifecycle.Event.ON_RESUME] 时静默刷新。
 * 未登录：仅在 [isSelected] 为 true 后首次进入或离开再进入时请求，避免一进首页就拉 `/v/api/dynamic/`。
 *
 * @param isSelected 当前是否处于首页「关注」Tab
 **/
@Composable
fun FollowView(
    viewModel: FollowViewModel = vortexaViewModel { FollowViewModel() },
    isSelected: Boolean = true,
) {
    val posts by viewModel.postList.collectAsState()
    val followingList by viewModel.followingList.collectAsState()
    val pageStatus by viewModel.pageStatus.collectAsState()
    val hasMorePosts by viewModel.hasMorePosts.collectAsState()
    val loadingMorePosts by viewModel.loadingMorePosts.collectAsState()
    val selectedFollowingUserId by viewModel.selectedFollowingUserId.collectAsState()
    val context = Context()
    val lifecycleOwner = LocalLifecycleOwner.current
    val dividerColor = Color(0xFFF3F4F5)
    val listState = rememberLazyListState()

    var wasHidden by remember { mutableStateOf(false) }
    var guestHasLoadedOnce by remember { mutableStateOf(false) }
    LaunchedEffect(isSelected) {
        if (!isSelected) {
            wasHidden = true
            return@LaunchedEffect
        }
        val loggedIn = TokenConfig.getToken().isNotEmpty()
        if (!loggedIn) {
            if (!guestHasLoadedOnce) {
                guestHasLoadedOnce = true
                viewModel.activateGuestFollowTab()
                viewModel.loadAllLists(showPageLoading = true)
            } else if (wasHidden) {
                viewModel.loadAllLists(showPageLoading = false)
            }
        } else {
            if (wasHidden) {
                viewModel.refreshOnResume()
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && TokenConfig.getToken().isNotEmpty()) {
                viewModel.refreshOnResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .belowStatusBar()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "关注",
                    style = FontMedium(fontSize = 18, color = Colors.black_101828)
                )
            }

            FollowedUserHorizontal(
                users = followingList,
                selectedUserId = selectedFollowingUserId,
                onUserClick = { user ->
                    viewModel.selectFollowingUser(user.userId)
                }
            )

            LazyColumn(
                state = listState,
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
                        onLikeClick = { viewModel.toggleLike(post.id) },
                        onBookmarkClick = { viewModel.toggleBookmark(post.id) },
                        onCommentClick = {
                            PostDetailActivity.start(context, post, openReplyComposer = true)
                        },
                        onPostClick = { PostDetailActivity.start(context, post) },
                        onModuleClick = { HomeCommunicateNavigation.startFromPost(context, post) }
                    )
                }

                if (pageStatus == PageStatus.Success && posts.isNotEmpty()) {
                    if (loadingMorePosts) {
                        item(key = "follow_load_more") {
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
                    } else if (!hasMorePosts) {
                        item(key = "follow_list_end") {
                            ListEndFooter(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            )
                        }
                    }
                }
            }
        }
        PageStatusView(
            status = pageStatus,
            modifier = Modifier.fillMaxSize(),
            emptyMessage = "暂无关注用户",
            showEmptyRefresh = true,
            onRefresh = { viewModel.loadAllLists(showPageLoading = true) }
        )
    }
}

@Composable
private fun FollowViewPreview() {
    FollowView()
}
