package com.vortexa.ui.page.systemmsg

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.config.UserConfig
import com.vortexa.ui.component.pageStatus.PageStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 系统通知页 UI 数据模型。
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

class SystemMessageViewModel(
    private val messageType: Int = SystemMessagePageType.SYSTEM,
    private val markReadOnEnterDialogId: Long? = null,
    private val markReadOnEnterMessageId: Long? = null,
    private val repository: SystemMessageRepository = SystemMessageRepository()
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 20
    }

    private var loadedPageNum = 1

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
                    Log.d(TAG, "mark read on enter: dialogId=$dialogId messageId=$messageId")
                }.onFailure {
                    Log.e(TAG, "mark read on enter failed", it)
                }
            }
        }
        refresh(showRefreshing = false)
    }

    fun refresh(showRefreshing: Boolean = true) {
        viewModelScope.launch {
            if (showRefreshing && _isRefreshing.value) return@launch
            if (showRefreshing) {
                _isRefreshing.value = true
            }
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
            }
        }
    }

    fun loadMessages() {
        refresh(showRefreshing = false)
    }

    private suspend fun loadFirstPage(showFullScreenLoading: Boolean, preserveItemsOnFailure: Boolean) {
        if (showFullScreenLoading) {
            _pageStatus.value = PageStatus.Loading
        }
        loadedPageNum = 1
        fetchMessagesPage(pageNum = 1, userId = UserConfig.getUserId())
            .onSuccess { response ->
                val list = response.list.map { mapToItem(it) }
                _items.value = list
                loadedPageNum = 1
                _hasMore.value = list.size < response.total
                _pageStatus.value = if (list.isEmpty()) PageStatus.Empty else PageStatus.Success
            }
            .onFailure {
                if (!preserveItemsOnFailure) {
                    _items.value = emptyList()
                }
                _hasMore.value = false
                _pageStatus.value = PageStatus.Fail
                Log.e(TAG, "loadFirstPage failed", it)
            }
    }

    fun loadMoreSystemMessages() {
        if (!_hasMore.value || _loadingMore.value) return
        if (_pageStatus.value != PageStatus.Success) return
        viewModelScope.launch {
            _loadingMore.value = true
            try {
                val nextPage = loadedPageNum + 1
                fetchMessagesPage(pageNum = nextPage, userId = UserConfig.getUserId())
                    .onSuccess { response ->
                        val newList = response.list.map { mapToItem(it) }
                        if (newList.isEmpty()) {
                            _hasMore.value = false
                            return@onSuccess
                        }
                        loadedPageNum = nextPage
                        val existingIds = _items.value.map { it.id }.toSet()
                        val merged = _items.value + newList.filter { it.id !in existingIds }
                        _items.value = merged
                        _hasMore.value = merged.size < response.total
                    }
                    .onFailure {
                        Log.e(TAG, "loadMoreSystemMessages failed", it)
                    }
            } finally {
                _loadingMore.value = false
            }
        }
    }

    private suspend fun fetchMessagesPage(pageNum: Int, userId: Long) =
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

    private fun mapToItem(it: com.vortexa.model.SystemNoticeItem): SystemMessageItem {
        val scheme = it.scheme?.takeIf { s -> s.isNotBlank() }
            ?: it.card?.scheme?.takeIf { s -> s.isNotBlank() }
        return SystemMessageItem(
            id = it.noticeId.toString(),
            title = listCategoryTitle,
            content = it.content,
            time = parseTimeDisplay(it.time),
            okButtonText = it.card?.title ?: "OK",
            cardUrl = it.card?.url,
            scheme = scheme
        )
    }

    private fun parseTimeDisplay(time: String?): String {
        if (time == null) return ""
        if (time.length < 16) return time
        return time.substring(11, 16)
    }

    fun onItemOkClick(item: SystemMessageItem) {
        Log.d(TAG, "onItemOkClick: id=${item.id}, scheme=${item.scheme}, cardUrl=${item.cardUrl}")
    }
}

private const val TAG = "SystemMessageVM"
