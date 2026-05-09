package com.vortexa.ui.page.systemmsg

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vortexa.ui.component.ListEndFooter
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.component.pageStatus.PageStatusView
import kotlinx.coroutines.flow.collect

/**
 * 系统通知 / 课堂小助手消息页：头部 + 列表。
 * [SystemMessagePageType.SYSTEM] 走 `/v/api/message/system`；[SystemMessagePageType.CLASSROOM_ASSISTANT] 走 `/v/api/message/classroom`。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemMessageView(
    messageType: Int = SystemMessagePageType.SYSTEM,
    markReadDialogId: Long? = null,
    markReadMessageId: Long? = null,
    viewModel: SystemMessageViewModel = viewModel(
        key = "system_message_${messageType}_${markReadDialogId}_$markReadMessageId",
        factory = SystemMessageViewModel.factory(messageType, markReadDialogId, markReadMessageId)
    ),
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    val headerTitle = when (messageType) {
        SystemMessagePageType.CLASSROOM_ASSISTANT -> "课堂小助手"
        else -> "系统通知"
    }
    val emptyMessage = when (messageType) {
        SystemMessagePageType.CLASSROOM_ASSISTANT -> "暂无课堂消息"
        else -> "暂无系统通知"
    }
    val items by viewModel.items.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pageStatus by viewModel.pageStatus.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val loadingMore by viewModel.loadingMore.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(listState, hasMore, loadingMore, pageStatus) {
        snapshotFlow {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = info.totalItemsCount
            lastVisible to total
        }.collect { (lastVisible, total) ->
            if (pageStatus == PageStatus.Success &&
                hasMore &&
                !loadingMore &&
                total > 0 &&
                lastVisible >= total - 2
            ) {
                viewModel.loadMoreSystemMessages()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        SystemMessageHeader(
            title = headerTitle,
            onBackClick = onBackClick,
            onMenuClick = onMenuClick
        )
        PullToRefreshBox(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(items, key = { it.id }) { item ->
                        SystemMessageListItem(
                            item = item,
                            onOkClick = { viewModel.onItemOkClick(item) }
                        )
                    }
                    if (pageStatus == PageStatus.Success && items.isNotEmpty()) {
                        if (loadingMore) {
                            item(key = "system_msg_load_more") {
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
                            item(key = "system_msg_list_end") {
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
                        pageStatus == PageStatus.Success && items.isEmpty() -> PageStatus.Empty
                        else -> pageStatus
                    },
                    modifier = Modifier.fillMaxSize(),
                    emptyMessage = emptyMessage,
                    showEmptyRefresh = true,
                    onRefresh = { viewModel.loadMessages() }
                )
            }
        }
    }
}

@Composable
@Preview
fun SystemMessagePreview() {
    SystemMessageView()
}
