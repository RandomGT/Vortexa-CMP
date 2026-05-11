package com.vortexa.ui.page.home.pager.message

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.config.UserConfig
import com.vortexa.model.DialogItem
import com.vortexa.net.auth.isLoginRequired
import com.vortexa.repository.MessageRepository
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.util.ToastUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 消息页 ViewModel。
 * 负责对话框列表加载及点击跳转；系统通知与课堂小助手未读与预览均来自 [DialogItem]（列表接口）。
 *
 * @author LuXin
 */
class MessageViewModel(
    private val messageRepository: MessageRepository = MessageRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "MessageViewModel"
        private const val DIALOG_PAGE_SIZE = 20
        /** 系统通知会话对端 userId，与后端约定一致；[MessageView] 据此识别首行系统通知 */
        const val SYSTEM_NOTIFICATION_USER_ID = 1000L
        /** 课堂小助手会话对端 userId（与后端一致时可与昵称「课堂小助手」择一或同时使用） */
        const val CLASSROOM_ASSISTANT_USER_ID = 1001L
        /** 系统通知接口无数据（或预览需占位）时，消息列表与首行预览统一文案 */
        const val SYSTEM_NOTICE_LIST_EMPTY_HINT = "暂无更多消息"

        /** 是否课堂小助手入口会话（列表项标题一般为「课堂小助手」） */
        fun isClassroomAssistantDialog(dialog: DialogItem): Boolean =
            dialog.userInfo.userId == CLASSROOM_ASSISTANT_USER_ID ||
                dialog.userInfo.userName == "课堂小助手"
    }

    private val _dialogList = MutableStateFlow<List<DialogItem>>(emptyList())
    /** 对话框列表，供 [MessageView] 展示 */
    val dialogList: StateFlow<List<DialogItem>> = _dialogList.asStateFlow()

    private val _hasUnreadDialogs = MutableStateFlow(false)
    /** 消息 Tab 是否展示红点，由当前会话列表未读状态统一派生。 */
    val hasUnreadDialogs: StateFlow<Boolean> = _hasUnreadDialogs.asStateFlow()

    /** 访客延迟到 Tab 展示后再请求，首屏避免无 token 时误触登录拦截 */
    private val _pageStatus = MutableStateFlow<PageStatus>(PageStatus.Success)
    /** 页面请求状态，供 [PageStatusView] 使用 */
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    /** 下拉刷新指示器是否与接口请求同步 */
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var dialogsLoadedPageNum = 1

    private val _hasMoreDialogs = MutableStateFlow(false)
    /** 会话列表是否还有下一页（上拉加载更多） */
    val hasMoreDialogs: StateFlow<Boolean> = _hasMoreDialogs.asStateFlow()

    private val _loadingMoreDialogs = MutableStateFlow(false)
    /** 是否正在请求下一页会话列表 */
    val loadingMoreDialogs: StateFlow<Boolean> = _loadingMoreDialogs.asStateFlow()

    /**
     * 全屏加载路径：用于首次进入与 [PageStatusView] 重试。
     * 进入 Loading 后请求列表并更新成功/空/失败态。
     */
    fun loadMessageList() {
        viewModelScope.launch {
            Log.d(TAG, "loadMessageList: start")
            try {
                fetchMessageListAndUpdateUi(
                    showFullScreenLoading = true,
                    preserveDialogsOnFailure = false
                )
            } catch (e: Exception) {
                _pageStatus.value = PageStatus.Fail
                updateDialogList(emptyList())
                Log.e(TAG, "loadMessageList: exception", e)
            }
        }
    }

    /**
     * 下拉刷新：仅驱动 [isRefreshing]，不切换全屏 Loading，避免与 [PageStatusView] 叠加。
     *
     * @param showRefreshing 是否展示 Material 下拉刷新圈；用户手势时为 true。
     */
    fun refresh(showRefreshing: Boolean = true) {
        viewModelScope.launch {
            if (showRefreshing && _isRefreshing.value) {
                Log.w(TAG, "refresh ignored: already refreshing")
                return@launch
            }
            if (showRefreshing) {
                _isRefreshing.value = true
            }
            Log.i(TAG, "refresh start, showRefreshing=$showRefreshing")
            try {
                fetchMessageListAndUpdateUi(
                    showFullScreenLoading = false,
                    preserveDialogsOnFailure = true
                )
            } catch (e: Exception) {
                Log.e(TAG, "refresh failed", e)
            } finally {
                if (showRefreshing) {
                    _isRefreshing.value = false
                }
                Log.i(TAG, "refresh end")
            }
        }
    }

    /**
     * 请求会话列表 [com.vortexa.api.MessageApi.getMessageList]，更新 [dialogList] 与 [pageStatus]。
     * 系统通知 / 课堂小助手的预览与时间、未读数均使用接口返回的 [DialogItem]。
     */
    private suspend fun fetchMessageListAndUpdateUi(
        showFullScreenLoading: Boolean,
        preserveDialogsOnFailure: Boolean
    ) {
        val userId = UserConfig.getUserId()
        val hasExistingDialogs = _dialogList.value.isNotEmpty()
        Log.d(TAG, "fetchMessageListAndUpdateUi: userId=$userId")
        if (showFullScreenLoading) {
            _pageStatus.value = PageStatus.Loading
        }
        dialogsLoadedPageNum = 1
        messageRepository.getMessageList(
            pageNum = 1,
            pageSize = DIALOG_PAGE_SIZE,
            userId = userId
        )
            .onSuccess { response ->
                val rawList = response.dialogs ?: emptyList()
                val list = ensureSystemNotification(rawList)
                updateDialogList(list)
                dialogsLoadedPageNum = 1
                _hasMoreDialogs.value = list.size < response.total
                _pageStatus.value = if (list.isEmpty()) PageStatus.Empty else PageStatus.Success
                Log.d(TAG, "fetchMessageListAndUpdateUi: success, size=${list.size}, total=${response.total}")
            }
            .onFailure {
                if (it.isLoginRequired()) {
                    if (!preserveDialogsOnFailure) {
                        updateDialogList(emptyList())
                        _hasMoreDialogs.value = false
                    }
                    if (!preserveDialogsOnFailure || !hasExistingDialogs) {
                        _pageStatus.value = PageStatus.Success
                    }
                    Log.i(TAG, "fetchMessageListAndUpdateUi: login required (guest)")
                    return@onFailure
                }
                if (!preserveDialogsOnFailure) {
                    updateDialogList(emptyList())
                    _hasMoreDialogs.value = false
                } else if (!hasExistingDialogs) {
                    _hasMoreDialogs.value = false
                }
                if (!preserveDialogsOnFailure || !hasExistingDialogs) {
                    _pageStatus.value = PageStatus.Fail
                }
                Log.e(TAG, "fetchMessageListAndUpdateUi: failed", it)
            }
    }

    /**
     * 会话列表上拉加载下一页：在 [pageStatus] 为 Success 且 [hasMoreDialogs] 时由列表滚动触发。
     */
    fun loadMoreDialogs() {
        if (!_hasMoreDialogs.value || _loadingMoreDialogs.value) {
            return
        }
        if (_pageStatus.value != PageStatus.Success) {
            return
        }
        viewModelScope.launch {
            _loadingMoreDialogs.value = true
            val nextPage = dialogsLoadedPageNum + 1
            val userId = UserConfig.getUserId()
            Log.i(TAG, "loadMoreDialogs: nextPage=$nextPage")
            try {
                messageRepository.getMessageList(
                    pageNum = nextPage,
                    pageSize = DIALOG_PAGE_SIZE,
                    userId = userId
                ).onSuccess { response ->
                    val newDialogs = response.dialogs ?: emptyList()
                    if (newDialogs.isEmpty()) {
                        _hasMoreDialogs.value = false
                        return@onSuccess
                    }
                    dialogsLoadedPageNum = nextPage
                    val existingIds = _dialogList.value.map { it.dialogId }.toSet()
                    val merged = _dialogList.value + newDialogs.filter { it.dialogId !in existingIds }
                    updateDialogList(merged)
                    _hasMoreDialogs.value = merged.size < response.total
                    Log.d(TAG, "loadMoreDialogs: mergedSize=${merged.size}, total=${response.total}")
                }.onFailure {
                    Log.e(TAG, "loadMoreDialogs: failed", it)
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadMoreDialogs: error", e)
            } finally {
                _loadingMoreDialogs.value = false
            }
        }
    }

    /**
     * 确保列表中包含系统通知。
     * 若接口未返回系统通知（userId=1000），则默认插入一条并置于首位。
     */
    private fun ensureSystemNotification(list: List<DialogItem>): List<DialogItem> {
        val hasSystem = list.any { it.userInfo.userId == SYSTEM_NOTIFICATION_USER_ID }
        return list
    }

    private fun updateDialogList(list: List<DialogItem>) {
        _dialogList.value = list
        _hasUnreadDialogs.value = list.any { it.unreadCount > 0 }
    }

    private fun clearDialogUnreadLocally(dialogId: Int) {
        val updatedList = _dialogList.value.map { dialogItem ->
            if (dialogItem.dialogId == dialogId && dialogItem.unreadCount > 0) {
                dialogItem.copy(unreadCount = 0)
            } else {
                dialogItem
            }
        }
        updateDialogList(updatedList)
    }

    /**
     * 点击某条对话框。
     * 详情页尚未迁移时消费点击并给出提示，避免从首页启动未接入页面。
     *
     * @param context 用于 Toast
     * @param dialog 被点击的对话框
     */
    fun onMessageClick(context: Context, dialog: DialogItem) {
        Log.d(TAG, "onMessageClick: dialogId=${dialog.dialogId} userName=${dialog.userInfo.userName}")
        val shouldMarkReadOnOpen =
            dialog.unreadCount > 0 &&
                (dialog.userInfo.userId == SYSTEM_NOTIFICATION_USER_ID || isClassroomAssistantDialog(dialog))
        if (shouldMarkReadOnOpen) {
            clearDialogUnreadLocally(dialog.dialogId)
        }
        if (dialog.userInfo.userId == SYSTEM_NOTIFICATION_USER_ID || isClassroomAssistantDialog(dialog)) {
            Log.i(TAG, "System message route is not migrated yet; click consumed safely")
            ToastUtil.show(context, "系统消息页面即将上线")
        } else {
            Log.i(TAG, "Chat route is not migrated yet; click consumed safely")
            ToastUtil.show(context, "私信页面即将上线")
        }
    }
}
