package com.vortexa.ui.page.profile.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vortexa.ui.component.pageStatus.PageStatusView

/**
 * 我的关注页面（Figma 504-50486）。
 * 头部 [MyFocusHeader] + 列表 [MyFocusItem]；通过 [PageStatusView] 展示加载/失败/空态（无关注时文案「暂无关注」，可刷新）。
 *
 * @param onBackClick 点击返回回调
 */
@Composable
fun MyFocusView(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MyFocusViewModel = viewModel()
) {
    val focusList by viewModel.focusList.collectAsState()
    val pageStatus by viewModel.pageStatus.collectAsState()
    val followLoading by viewModel.followLoading.collectAsState()
    val unfollowLoading by viewModel.unfollowLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFocusList()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MyFocusHeader(onBackClick = onBackClick)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(focusList, key = { it.userId }) { user ->
                        MyFocusItem(
                            userId = user.userId,
                            nickname = user.nickname,
                            bio = user.bio,
                            isFollowing = user.isFollowing,
                            followLoading = followLoading,
                            unfollowLoading = unfollowLoading,
                            onFollowClick = { viewModel.follow(user.userId) },
                            onUnfollowConfirm = { viewModel.unfollow(user.userId) },
                            onItemClick = { /* TODO: 跳转用户详情 */ }
                        )
                    }
                }

                PageStatusView(
                    status = pageStatus,
                    modifier = Modifier.fillMaxSize(),
                    emptyMessage = "暂无关注",
                    showEmptyRefresh = true,
                    onRefresh = { viewModel.loadFocusList() }
                )
            }
        }
    }
}

/** 关注用户数据 */
data class FocusUser(
    val userId: Long,
    val nickname: String,
    val bio: String,
    val isFollowing: Boolean
)

@Composable
fun MyFocusPreview() {
    MyFocusView()
}
