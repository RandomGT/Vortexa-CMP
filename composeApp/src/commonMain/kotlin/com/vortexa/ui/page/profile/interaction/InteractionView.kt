package com.vortexa.ui.page.profile.interaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.unit.dp
import com.vortexa.ui.viewmodel.vortexaViewModel
import kotlinx.coroutines.launch
import com.vortexa.model.InteractionListItem
import com.vortexa.ui.component.ListEndFooter
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.page.post.detail.PostDetailActivity
import com.vortexa.ui.theme.belowStatusBar

/**
 * 互动管理页：头部 + 筛选（所有人/我的关注）+ TabBar（回复/点赞）+ 互动类型浮层 + 列表。
 * 列表数据来自 POST /v/api/user/interactions，筛选项映射：actorType、actionType、direction。
 *
 * @param onBackClick 点击头部返回回调
 * @param onMenuClick 点击头部右侧菜单回调（内部已处理浮层，此参数保留兼容）
 */
@Composable
fun InteractionView(
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    viewModel: InteractionViewModel = vortexaViewModel { InteractionViewModel() },
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { InteractionTabs.size }
    )
    val scope = rememberCoroutineScope()
    var showTypeFilterPopup by remember { mutableStateOf(false) }

    val actorType by viewModel.actorType.collectAsState()
    val replyList by viewModel.replyInteractionList.collectAsState()
    val likeList by viewModel.likeInteractionList.collectAsState()
    val replyHasMore by viewModel.replyHasMore.collectAsState()
    val likeHasMore by viewModel.likeHasMore.collectAsState()
    val replyLoadingMore by viewModel.replyLoadingMore.collectAsState()
    val likeLoadingMore by viewModel.likeLoadingMore.collectAsState()
    val pageStatus by viewModel.pageStatus.collectAsState()
    val direction by viewModel.direction.collectAsState()

    LaunchedEffect(pagerState.settledPage) {
        val actionType = if (pagerState.settledPage == 0) 1 else 0
        viewModel.setActionType(actionType)
    }
    val selectedTypeIndex = when (direction) {
        0 -> 0
        1 -> 2
        2 -> 1
        else -> 0
    }

    val typeFilterOptions = remember(pagerState.settledPage) {
        if (pagerState.settledPage == 0) InteractionTypeOptionsReply else InteractionTypeOptionsLike
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .belowStatusBar()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            InteractionHeader(
                onBackClick = onBackClick,
                onMenuClick = { showTypeFilterPopup = true }
            )

            InteractionFilter(
                labels = listOf("所有人", "我的关注"),
                selectedIndex = actorType,
                onChipClick = { viewModel.setActorType(it) }
            )

            Box(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomStart
            ) {
                InteractionTabBar(
                    selectedIndex = pagerState.settledPage,
                    onTabClick = { index ->
                        scope.launch { pagerState.animateScrollToPage(index) }
                        viewModel.setActionType(if (index == 0) 1 else 0)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 4.dp, start = 18.dp)
                )
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = true,
                    beyondViewportPageCount = InteractionTabs.size,
                    pageContent = { page ->
                        key(page) {
                            InteractionPageContent(
                                page = page,
                                list = if (page == 0) replyList else likeList,
                                pageStatus = pageStatus,
                                hasMore = if (page == 0) replyHasMore else likeHasMore,
                                loadingMore = if (page == 0) replyLoadingMore else likeLoadingMore,
                                onLoadMore = if (page == 0) {
                                    { viewModel.loadMoreReplyInteractions() }
                                } else {
                                    { viewModel.loadMoreLikeInteractions() }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White)
                            )
                        }
                    }
                )
                PageStatusView(
                    status = pageStatus,
                    modifier = Modifier.fillMaxSize(),
                    onRefresh = { viewModel.loadInteractions() }
                )
            }
        }

        if (showTypeFilterPopup) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showTypeFilterPopup = false },
                contentAlignment = Alignment.TopEnd
            ) {
                InteractionTypeFilterPopup(
                    options = typeFilterOptions,
                    selectedIndex = selectedTypeIndex,
                    onOptionClick = { index ->
                        val dir = when (index) {
                            0 -> 0
                            1 -> 2
                            2 -> 1
                            else -> 0
                        }
                        viewModel.setDirection(dir)
                        showTypeFilterPopup = false
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 50.dp, end = 18.dp)
                )
            }
        }
    }
}

/**
 * 单页内容：回复列表或点赞列表，根据 [InteractionListItem] 渲染。
 *
 * @param page 0=回复，1=点赞
 * @param list 当前 Tab 的互动列表（接口返回）
 * @param hasMore 是否还有下一页
 * @param loadingMore 是否正在加载下一页
 * @param onLoadMore 列表接近底部时加载更多
 */
@Composable
private fun InteractionPageContent(
    page: Int,
    list: List<InteractionListItem>,
    pageStatus: PageStatus,
    hasMore: Boolean,
    loadingMore: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(listState, hasMore, loadingMore, pageStatus, list.size) {
        snapshotFlow {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = info.totalItemsCount
            lastVisible to total
        }.collect { (lastVisible, total) ->
            if (pageStatus == PageStatus.Success &&
                hasMore &&
                !loadingMore &&
                list.isNotEmpty() &&
                total > 0 &&
                lastVisible >= total - 2
            ) {
                onLoadMore()
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        if (page == 0) {
            itemsIndexed(
                list,
                key = { index, item -> "${index}_${item.interactionLazyKey()}" }
            ) { _, item ->
                val replyTo = when (item.type) {
                    1 -> "你的贴文"
                    2 -> "你的动态"
                    else -> "你"
                }
                InteractionReplyItem(
                    nickname = item.userName,
                    time = item.time,
                    replyTo = replyTo,
                    content = item.typeData,
                    userAvatar = item.userAvatar.takeIf { it.isNotBlank() },
                    onClick = if (item.postId != 0L) {
                        { PostDetailActivity.start(context, item.postId.toString()) }
                    } else null
                )
            }
        } else {
            itemsIndexed(
                list,
                key = { index, item -> "${index}_${item.interactionLazyKey()}" }
            ) { _, item ->
                val subtitle = when (item.type) {
                    1 -> "赞了你的帖子"
                    2 -> "赞了你的评论"
                    else -> "赞了你"
                }
                InteractionLikeItem(
                    nickname = item.userName,
                    time = item.time,
                    subtitle = subtitle,
                    userAvatar = item.userAvatar.takeIf { it.isNotBlank() },
                    onClick = if (item.postId != 0L) {
                        { PostDetailActivity.start(context, item.postId.toString()) }
                    } else null
                )
            }
        }

        if (pageStatus == PageStatus.Success && list.isNotEmpty()) {
            if (loadingMore) {
                item(key = "interaction_load_more_$page") {
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
            } else if (!hasMore) {
                item(key = "interaction_list_end_$page") {
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

/** LazyColumn item 业务侧稳定 key；最终 key 仍带列表下标，防止接口返回完全相同的重复行。 */
private fun InteractionListItem.interactionLazyKey(): String =
    "${userId}_${postId}_${time}_${type}_${action}_${typeData}"

@Composable
private fun InteractionViewPreview() {
    InteractionView(onBackClick = {}, onMenuClick = {})
}
