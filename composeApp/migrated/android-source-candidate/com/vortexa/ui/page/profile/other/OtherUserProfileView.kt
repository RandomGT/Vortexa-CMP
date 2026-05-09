package com.vortexa.ui.page.profile.other

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vortexa.config.UserConfig
import com.vortexa.ui.component.LoadingButton
import com.vortexa.ui.theme.BaseTheme
import com.vortexa.ui.theme.Colors
import kotlinx.coroutines.launch

/**
 * 他人个人主页（Figma 415-41955）。根据 [userId] 请求 `/v/api/user/profile/{userId}`；
 * 关注/粉丝下方为发帖、回复 Tab + 列表（分页接口见 [OtherUserProfileViewModel]）。
 */
@Composable
fun OtherUserProfileView(
    userId: Long,
    onBackClick: () -> Unit = {},
) {
    val viewModel = viewModel<OtherUserProfileViewModel>()
    val profile by viewModel.profile.collectAsState()
    val followLoading by viewModel.followLoading.collectAsState()
    val userPosts by viewModel.userPosts.collectAsState()
    val postsPageStatus by viewModel.postsPageStatus.collectAsState()
    val postsHasMore by viewModel.postsHasMore.collectAsState()
    val postsLoadingMore by viewModel.postsLoadingMore.collectAsState()
    val userComments by viewModel.userComments.collectAsState()
    val commentsPageStatus by viewModel.commentsPageStatus.collectAsState()
    val commentsHasMore by viewModel.commentsHasMore.collectAsState()
    val commentsLoadingMore by viewModel.commentsLoadingMore.collectAsState()

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { OtherUserProfileTabs.size }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
        viewModel.onFeedUserIdChanged(userId)
    }

    LaunchedEffect(pagerState.settledPage) {
        selectedTabIndex = pagerState.settledPage
    }

    LaunchedEffect(pagerState.settledPage, userId) {
        if (pagerState.settledPage == 1 && userId > 0L) {
            viewModel.loadUserCommentsFirstPage(userId)
        }
    }

    val info = profile?.userInfo
    val isSelf = info?.userId != null && info.userId == UserConfig.getUserId()

    BaseTheme(belowStatusBar = false, aboveNavigationBar = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                OtherUserProfileHeader(
                    onBackClick = onBackClick,
                    avatarUrl = info?.avatar,
                    nickname = info?.nickname.orEmpty(),
                    isVerified = info?.isVerified == true,
                    tags = certificationsToHeaderTags(info?.certifications)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    OtherUserProfileStats(
                        modifier = Modifier.padding(top = 24.dp, bottom = 16.dp),
                        followCount = info?.followCount ?: 0,
                        fanCount = info?.fanCount ?: 0
                    )
                }
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    OtherUserProfileTabBar(
                        selectedIndex = selectedTabIndex,
                        onTabClick = { index ->
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                            selectedTabIndex = index
                        },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 4.dp)
                    )
                }
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    beyondViewportPageCount = 0,
                    pageContent = { page ->
                        when (page) {
                            0 -> OtherUserProfilePostsTab(
                                posts = userPosts,
                                pageStatus = postsPageStatus,
                                hasMore = postsHasMore,
                                loadingMore = postsLoadingMore,
                                onLoadMore = { viewModel.loadUserPostsNextPage() },
                                onRefresh = { viewModel.loadUserPostsFirstPage(userId) },
                                onLikeClick = { viewModel.togglePostLike(it) },
                                onBookmarkClick = { viewModel.togglePostBookmark(it) },
                                modifier = Modifier.fillMaxSize()
                            )

                            else -> OtherUserProfileRepliesTab(
                                comments = userComments,
                                pageStatus = commentsPageStatus,
                                hasMore = commentsHasMore,
                                loadingMore = commentsLoadingMore,
                                onLoadMore = { viewModel.loadUserCommentsNextPage() },
                                onRefresh = {
                                    viewModel.loadUserCommentsFirstPage(
                                        userId,
                                        force = true
                                    )
                                },
                                onCommentLike = { viewModel.toggleCommentLike(it) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                )
            }
            if (!isSelf && info != null) {
                val followed = profile?.isFollowed == true
                LoadingButton(
                    modifier = Modifier
                        .padding(end = 16.dp, top = 26.dp)
                        .width(68.dp)
                        .height(30.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            Color.White,
                            RoundedCornerShape(24.dp)
                        ),
                    text = if (followed) "已关注" else "关注",
                    isLoading = followLoading,
                    textColor = Colors.black_101828,
                    loadingIndicatorColor = Colors.black_101828,
                    onClick = { viewModel.toggleFollow() }
                )
            }
        }
    }
}

@Composable
@Preview
private fun OtherUserProfileViewPreview() {
    BaseTheme {
        OtherUserProfileView(userId = 1L)
    }
}
