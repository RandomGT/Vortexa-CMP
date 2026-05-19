package com.vortexa.ui.page.profile.paper.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.repository.UserRepository
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.util.ToastUtil
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 稿件管理页 ViewModel。
 * 负责列表加载、筛选与删除。
 */
class PaperManagementViewModel(
    private val repository: PaperManagementRepository = PaperManagementRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    val paperFilters = listOf("全部", "草稿箱", "发布成功")

    private val _selectedFilter = MutableStateFlow(0)
    val selectedFilter: StateFlow<Int> = _selectedFilter.asStateFlow()

    private val _paperList = MutableStateFlow<List<PaperItemData>>(emptyList())
    val paperList: StateFlow<List<PaperItemData>> = _paperList.asStateFlow()

    private val _pageStatus = MutableStateFlow<PageStatus>(PageStatus.Loading)
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    private val _deletingPostId = MutableStateFlow(0L)
    val deletingPostId: StateFlow<Long> = _deletingPostId.asStateFlow()

    private val _deletePostFinished = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val deletePostFinished: SharedFlow<Unit> = _deletePostFinished.asSharedFlow()

    init {
        loadPosts()
    }

    fun loadPosts(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _pageStatus.value = PageStatus.Loading
            }
            val statusIndex = _selectedFilter.value
            val status = if (statusIndex == 0) null else statusIndex
            repository.getPosts(status = status)
                .onSuccess { response ->
                    val items = response.list.map { repository.mapToPaperItemData(it) }
                    _paperList.value = items
                    _pageStatus.value = if (items.isEmpty()) PageStatus.Empty else PageStatus.Success
                }
                .onFailure { error ->
                    if (!silent) {
                        _pageStatus.value = PageStatus.Fail
                        _paperList.value = emptyList()
                    }
                    ToastUtil.show(error.message ?: "加载失败")
                }
        }
    }

    fun onFilterClick(index: Int) {
        if (_selectedFilter.value == index) return
        _selectedFilter.value = index
        loadPosts()
    }

    fun deletePost(postId: Long) {
        if (postId <= 0L) return
        viewModelScope.launch {
            _deletingPostId.value = postId
            try {
                userRepository.deletePost(postId)
                    .onSuccess { data ->
                        ToastUtil.show(data.msg?.takeIf { it.isNotBlank() } ?: "删除成功")
                        val next = _paperList.value.filterNot { it.postId == postId }
                        _paperList.value = next
                        if (next.isEmpty()) {
                            _pageStatus.value = PageStatus.Empty
                        }
                        _deletePostFinished.emit(Unit)
                    }
                    .onFailure { error ->
                        ToastUtil.show(error.message ?: "删除失败")
                    }
            } finally {
                _deletingPostId.value = 0L
            }
        }
    }
}
