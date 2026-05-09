package com.vortexa.ui.page.systemmsg

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vortexa.config.UserConfig
import com.vortexa.repository.MessageRepository
import com.vortexa.ui.component.pageStatus.PageStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import com.vortexa.model.SystemMessageListResponse

/**
 * 系统通知页 UI 数据模型。
 *
 * @param id 唯一标识（对应 noticeId）
 * @param title 分类标题，接口未提供时使用「系统通知」
 * @param content 通知正文
 * @param time 展示时间，如 09:12
 * @param okButtonText OK 按钮文案，来自 card.title，默认「OK」
 * @param cardUrl 卡片跳转路径，可为 null
 * @param scheme 后端下发的 `vortexa://` 完整 URI，点击 OK 时在 [SystemMessageListItem] 中走路由
 */
data class SystemMessageItem(
    val id: String,
    val title: String,
    val content: String,
    val time: String = "",
    val okButtonText: String = "OK",
    val cardUrl: String? = null,
    val scheme: String? = null
)

/**
 * 系统通知页 ViewModel。
 * [SystemMessagePageType.SYSTEM] 使用 [MessageRepository.getSystemMessages]；
 * [SystemMessagePageType.CLASSROOM_ASSISTANT] 使用 [MessageRepository.getClassroomMessages]。
 */
class SystemMessageViewModel(
    private val messageType: Int = SystemMessagePageType.SYSTEM,
    private val markReadOnEnterDialogId: Long? = null,
    private val markReadOnEnterMessageId: Long? = null,
    private val repository: MessageRepository = MessageRepository()
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 20

        fun factory(
            messageType: Int,
            markReadOnEnterDialogId: Long? = null,
            markReadOnEnterMessageId: Long? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(SystemMessageViewModel::class.java))
                return SystemMessageViewModel(
                    messageType = messageType,
                    markReadOnEnterDialogId = markReadOnEnterDialogId,
                    markReadOnEnterMessageId = markReadOnEnterMessageId
                ) as T
            }
        }
    }

    private var systemLoadedPageNum = 1

    private val _items = MutableStateFlow<List<SystemMessageItem>>(emptyList())
    val items: StateFlow<List<SystemMessageItem>> = _items.asStateFlow()

    private val _pageStatus = MutableStateFlow<PageStatus>(PageStatus.Loading)
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _loadingMore = MutableStateFlow(false)
    val loadingMore: StateFlow<Boolean> = _loadingMore.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        val dialogId = markReadOnEnterDialogId
        val messageId = markReadOnEnterMessageId
        if (dialogId != null && messageId != null && dialogId > 0 && messageId > 0) {
            viewModelScope.launch {
                val userId = UserConfig.getUserId()
                repository.batchMarkRead(
                    userId = userId,
                    dialogIds = dialogId.toString(),
                    messageIds = messageId.toString()
                ).onSuccess {
                    Log.d(TAG, "mark read on enter: dialogId=$dialogId messageId=$messageId count=$it")
                }.onFailure {
                    Log.e(TAG, "mark read on enter failed", it)
                }
            }
        }
        refresh(showRefreshing = false)
    }

    /**
     * 下拉刷新或全页重试时拉取第一页数据。
     *
     * @param showRefreshing 为 true 时展示下拉刷新指示，且不先进入全屏 Loading；为 false 时与首次进入 / 空态重试一致。
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
                loadFirstPage(showFullScreenLoading = !showRefreshing, preserveItemsOnFailure = showRefreshing)
            } catch (e: CancellationException) {
                throw e
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
     * 与空态 / 失败态「点击刷新」一致：全屏 Loading 后拉第一页。
     */
    fun loadMessages() {
        refresh(showRefreshing = false)
    }

    private suspend fun loadFirstPage(showFullScreenLoading: Boolean, preserveItemsOnFailure: Boolean) {
        if (showFullScreenLoading) {
            _pageStatus.value = PageStatus.Loading
        }
        systemLoadedPageNum = 1
        Log.d(TAG, "loadFirstPage: start, showFullScreenLoading=$showFullScreenLoading")
        val userId = UserConfig.getUserId()
        fetchMessagesPage(pageNum = 1, userId = userId)
            .onSuccess { response ->
                val list = response.list.map { mapToItem(it) }
                _items.value = list
                systemLoadedPageNum = 1
                _hasMore.value = list.size < response.total
                _pageStatus.value = if (list.isEmpty()) PageStatus.Empty else PageStatus.Success
                Log.d(TAG, "loadFirstPage: success, size=${list.size}, total=${response.total}")
            }
            .onFailure {
                if (!preserveItemsOnFailure) {
                    _items.value = emptyList()
                }
                _hasMore.value = false
                _pageStatus.value = PageStatus.Fail
                Log.e(TAG, "loadFirstPage: failed", it)
            }
    }

    /**
     * 系统通知列表上拉加载下一页。
     */
    fun loadMoreSystemMessages() {
        if (!_hasMore.value || _loadingMore.value) return
        if (_pageStatus.value != PageStatus.Success) return
        viewModelScope.launch {
            _loadingMore.value = true
            val nextPage = systemLoadedPageNum + 1
            val userId = UserConfig.getUserId()
            Log.i(TAG, "loadMoreSystemMessages: nextPage=$nextPage")
            try {
                fetchMessagesPage(pageNum = nextPage, userId = userId)
                    .onSuccess { response ->
                        val newList = response.list.map { mapToItem(it) }
                        if (newList.isEmpty()) {
                            _hasMore.value = false
                            return@onSuccess
                        }
                        systemLoadedPageNum = nextPage
                        val existingIds = _items.value.map { it.id }.toSet()
                        val merged = _items.value + newList.filter { it.id !in existingIds }
                        _items.value = merged
                        _hasMore.value = merged.size < response.total
                        Log.d(TAG, "loadMoreSystemMessages: mergedSize=${merged.size}, total=${response.total}")
                    }
                    .onFailure {
                        Log.e(TAG, "loadMoreSystemMessages: failed", it)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "loadMoreSystemMessages: error", e)
            } finally {
                _loadingMore.value = false
            }
        }
    }

    private suspend fun fetchMessagesPage(pageNum: Int, userId: Long): Result<SystemMessageListResponse> =
        when (messageType) {
            SystemMessagePageType.CLASSROOM_ASSISTANT ->
                repository.getClassroomMessages(pageNum = pageNum, pageSize = PAGE_SIZE, userId = userId)
            else -> repository.getSystemMessages(pageNum = pageNum, pageSize = PAGE_SIZE, userId = userId)
        }

    private val listCategoryTitle: String
        get() = when (messageType) {
            SystemMessagePageType.CLASSROOM_ASSISTANT -> "课堂小助手"
            else -> "系统通知"
        }

    /**
     * 将接口数据映射为 UI 模型。
     */
    private fun mapToItem(it: com.vortexa.model.SystemNoticeItem): SystemMessageItem {
        val timeDisplay = parseTimeDisplay(it.time)
        val scheme = it.scheme?.takeIf { s -> s.isNotBlank() }
            ?: it.card?.scheme?.takeIf { s -> s.isNotBlank() }
        return SystemMessageItem(
            id = it.noticeId.toString(),
            title = listCategoryTitle,
            content = it.content,
            time = timeDisplay,
            okButtonText = it.card?.title ?: "OK",
            cardUrl = it.card?.url,
            scheme = scheme
        )
    }

    /**
     * 从 "2026-01-21 09:12:33" 解析出 "09:12"。
     */
    private fun parseTimeDisplay(time: String?): String {
        if (time==null) return ""
        if (time.length < 16) return time
        return time.substring(11, 16) // HH:mm
    }

    /**
     * 点击某条通知的 OK 按钮；scheme 跳转见 [SystemMessageListItem]，此处可扩展已读同步等。
     */
    fun onItemOkClick(item: SystemMessageItem) {
        Log.d(TAG, "onItemOkClick: id=${item.id}, scheme=${item.scheme}, cardUrl=${item.cardUrl}")
    }
}

private const val TAG = "SystemMessageVM"
