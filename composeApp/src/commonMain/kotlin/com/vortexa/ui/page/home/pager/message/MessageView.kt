package com.vortexa.ui.page.home.pager.message

import android.content.Context
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
import android.util.Log
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
import com.vortexa.config.TokenConfig
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.ui.component.ListEndFooter
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.theme.belowStatusBar
import kotlinx.coroutines.flow.collect

/**
 * 消息页：头部（私信+加号）、搜索栏（搜索好友）、消息列表。
 * 通过 /v/api/message/list 加载对话框列表，使用 [PageStatusView] 展示加载/失败/空状态。
 * 「系统通知」「课堂小助手」的预览、时间与未读角标均来自列表项 [com.vortexa.model.DialogItem]（含 unreadCount）。
 * 已登录：随宿主 Activity 每次 [Lifecycle.Event.ON_RESUME] 静默刷新（便于红点与列表）。
 * 未登录：仅在 [isSelected] 为 true 时首次进入或离开后再进入时请求，避免一进首页就拉消息接口触发登录页。
 *
 * @param isSelected 当前是否处于首页「消息」Tab
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MessageView(
    viewModel: MessageViewModel = vortexaViewModel { MessageViewModel() },
    isSelected: Boolean = true,
) {
    val context = Context()
    val lifecycleOwner = LocalLifecycleOwner.current
    val dialogList by viewModel.dialogList.collectAsState()
    val pageStatus by viewModel.pageStatus.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val hasMoreDialogs by viewModel.hasMoreDialogs.collectAsState()
    val loadingMoreDialogs by viewModel.loadingMoreDialogs.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
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
                viewModel.loadMessageList()
            } else if (wasHidden) {
                viewModel.refresh(showRefreshing = false)
            }
        } else {
            if (wasHidden) {
                viewModel.refresh(showRefreshing = false)
            }
        }
    }

    LaunchedEffect(listState, hasMoreDialogs, loadingMoreDialogs, pageStatus) {
        snapshotFlow {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = info.totalItemsCount
            lastVisible to total
        }.collect { (lastVisible, total) ->
            if (pageStatus == PageStatus.Success &&
                hasMoreDialogs &&
                !loadingMoreDialogs &&
                total > 0 &&
                lastVisible >= total - 2
            ) {
                viewModel.loadMoreDialogs()
            }
        }
    }

    /**
     * HomeActivity 每次回到前台（onResume）时静默拉取消息列表，便于从聊天等页面返回后数据及时更新。
     * 不展示下拉刷新圈，避免与全屏 Loading / 列表闪烁冲突。
     */
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && TokenConfig.getToken().isNotEmpty()) {
                Log.d("MessageView", "ON_RESUME: refresh message list (silent), logged in")
                viewModel.refresh(showRefreshing = false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    PullToRefreshBox(
        modifier = Modifier
            .fillMaxSize()
            .belowStatusBar()
            .background(Color.White),
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() }
    ) {
    Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            MessagePageHeader(onAddClick = { })
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .fillMaxWidth()
                ) {
                    items(dialogList, key = { it.dialogId }) { dialog ->
                        val isSystemNotice =
                            dialog.userInfo.userId == MessageViewModel.SYSTEM_NOTIFICATION_USER_ID
                        val isClassroomAssistant =
                            MessageViewModel.isClassroomAssistantDialog(dialog)
                        val rowPreview = messageRowPreview(
                            isSystemNotice = isSystemNotice,
                            isClassroomAssistant = isClassroomAssistant,
                            dialogPreview = dialog.lastMessage.content,
                        )
                        val rowTime = dialog.lastMessage.sendTime
                        MessageListItem(
                            title = dialog.userInfo.userName,
                            time = rowTime,
                            preview = rowPreview,
                            unreadCount = dialog.unreadCount,
                            onClick = { viewModel.onMessageClick(context, dialog) }
                        )
                    }
                    if (pageStatus == PageStatus.Success && dialogList.isNotEmpty()) {
                        if (loadingMoreDialogs) {
                            item(key = "message_load_more") {
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
                        } else if (!hasMoreDialogs) {
                            item(key = "message_list_end") {
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
                    emptyMessage = "暂无消息",
                    showEmptyRefresh = true,
                    onRefresh = { viewModel.loadMessageList() }
                )
            }
        }
    }
}

/**
 * 计算消息列表单行预览：系统通知 / 课堂小助手无正文时用 [MessageViewModel.SYSTEM_NOTICE_LIST_EMPTY_HINT]。
 */
private fun messageRowPreview(
    isSystemNotice: Boolean,
    isClassroomAssistant: Boolean,
    dialogPreview: String?,
): String {
    if (!isSystemNotice && !isClassroomAssistant) return dialogPreview ?: ""
    val fallback = dialogPreview?.takeIf { it.isNotBlank() }
    return fallback ?: MessageViewModel.SYSTEM_NOTICE_LIST_EMPTY_HINT
}

@Composable
fun MessagePreview() {
    com.vortexa.ui.theme.BaseTheme { MessageView() }
}
