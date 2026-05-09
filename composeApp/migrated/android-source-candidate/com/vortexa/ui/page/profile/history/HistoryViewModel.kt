package com.vortexa.ui.page.profile.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.model.Post
import com.vortexa.repository.UserRepository
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.page.profile.collection.collectionFilterApiModuleParams
import com.vortexa.ui.page.post.detail.PostDetailSyncCenter
import com.vortexa.ui.page.post.detail.applyPostDetailSync
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * 浏览记录页 ViewModel。
 * 负责浏览记录加载（GET /v/api/user/viewHistory）、筛选及点赞/收藏交互。
 *
 * @author LuXin
 */
class HistoryViewModel(
    private val repository: HistoryRepository = HistoryRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _postList = MutableStateFlow<List<Post>>(emptyList())
    val postList: StateFlow<List<Post>> = _postList.asStateFlow()

    /** 当前选中的分区：0=全部，1=杂谈，2=交易经验，3=玩法 */
    private val _selectedFilterIndex = MutableStateFlow(0)
    val selectedFilterIndex: StateFlow<Int> = _selectedFilterIndex.asStateFlow()

    private val _pageStatus = MutableStateFlow<PageStatus>(PageStatus.Loading)
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    private val pageSize = 20
    private var historyPageNum = 1

    private val _hasMoreHistory = MutableStateFlow(false)
    val hasMoreHistory: StateFlow<Boolean> = _hasMoreHistory.asStateFlow()

    private val _loadingMoreHistory = MutableStateFlow(false)
    val loadingMoreHistory: StateFlow<Boolean> = _loadingMoreHistory.asStateFlow()

    init {
        observePostDetailSync()
        loadHistory()
    }

    /**
     * 监听帖子详情页的变更事件，并将最新状态合并到浏览记录列表。
     * @return 无返回值。
     */
    private fun observePostDetailSync() {
        viewModelScope.launch {
            PostDetailSyncCenter.events.collect { event ->
                val current = _postList.value
                val updated = current.applyPostDetailSync(event)
                if (updated !== current) {
                    _postList.value = updated
                    Log.d(TAG, "observePostDetailSync: merged postId=${event.postId}")
                }
            }
        }
    }

    /**
     * 加载浏览记录。
     */
    fun loadHistory() {
        viewModelScope.launch {
            _pageStatus.value = PageStatus.Loading
            historyPageNum = 1
            val index = _selectedFilterIndex.value
            val module = collectionFilterApiModuleParams.getOrElse(index) { null }
            Log.d(TAG, "loadHistory: module=$module")
            repository.getViewHistory(
                module = module,
                pageNum = 1,
                pageSize = pageSize
            )
                .onSuccess { response ->
                    val posts = response.list.map { repository.mapToPost(it) }
                    _postList.value = posts
                    historyPageNum = 1
                    _hasMoreHistory.value = posts.size < response.total
                    _pageStatus.value = if (posts.isEmpty()) PageStatus.Empty else PageStatus.Success
                    Log.d(TAG, "loadHistory: success, size=${posts.size}, total=${response.total}")
                }
                .onFailure {
                    _pageStatus.value = PageStatus.Fail
                    _postList.value = emptyList()
                    _hasMoreHistory.value = false
                    Log.e(TAG, "loadHistory: failed", it)
                }
        }
    }

    /**
     * 上拉加载更多浏览记录（下一页追加）。
     */
    fun loadMoreHistory() {
        if (!_hasMoreHistory.value || _loadingMoreHistory.value) return
        if (_pageStatus.value != PageStatus.Success) return
        viewModelScope.launch {
            _loadingMoreHistory.value = true
            val index = _selectedFilterIndex.value
            val module = collectionFilterApiModuleParams.getOrElse(index) { null }
            val nextPage = historyPageNum + 1
            Log.d(TAG, "loadMoreHistory: module=$module, page=$nextPage")
            repository.getViewHistory(
                module = module,
                pageNum = nextPage,
                pageSize = pageSize
            )
                .onSuccess { response ->
                    val newPosts = response.list.map { repository.mapToPost(it) }
                    if (newPosts.isEmpty()) {
                        _hasMoreHistory.value = false
                    } else {
                        historyPageNum = nextPage
                        val merged = _postList.value + newPosts
                        _postList.value = merged
                        _hasMoreHistory.value = merged.size < response.total
                    }
                    Log.d(TAG, "loadMoreHistory: added=${newPosts.size}, hasMore=${_hasMoreHistory.value}")
                }
                .onFailure {
                    _hasMoreHistory.value = false
                    Log.e(TAG, "loadMoreHistory: failed", it)
                }
            _loadingMoreHistory.value = false
        }
    }

    /**
     * 设置筛选分区并重新加载。
     * @param index 与 [collectionFilterApiModuleParams] 下标一致
     */
    fun setFilter(index: Int) {
        if (_selectedFilterIndex.value == index) return
        _selectedFilterIndex.value = index
        loadHistory()
    }

    /**
     * 切换点赞状态，调用点赞/取消点赞接口。
     */
    fun toggleLike(postId: String) {
        val postIdLong = postId.toLongOrNull() ?: return
        val list = _postList.value.toMutableList()
        val idx = list.indexOfFirst { it.id == postId }
        if (idx < 0) return
        val post = list[idx]
        viewModelScope.launch {
            val result = if (post.isLiked) userRepository.unlikePost(postIdLong) else userRepository.likePost(postIdLong)
            result
                .onSuccess {
                    val index = _postList.value.indexOfFirst { it.id == postId }
                    if (index >= 0) {
                        val p = _postList.value[index]
                        val newList = _postList.value.toMutableList()
                        newList[index] = p.copy(
                            isLiked = !p.isLiked,
                            likeCount = (p.likeCount + if (p.isLiked) -1 else 1).coerceAtLeast(0)
                        )
                        _postList.value = newList
                    }
                    Log.d(TAG, "toggleLike: postId=$postId success")
                }
                .onFailure { Log.e(TAG, "toggleLike: postId=$postId failed", it) }
        }
    }

    /**
     * 切换收藏状态，调用收藏/取消收藏接口。
     */
    fun toggleBookmark(postId: String) {
        val postIdLong = postId.toLongOrNull() ?: return
        val list = _postList.value.toMutableList()
        val idx = list.indexOfFirst { it.id == postId }
        if (idx < 0) return
        val post = list[idx]
        viewModelScope.launch {
            val result = if (post.isCollect) userRepository.uncollectPost(postIdLong) else userRepository.collectPost(postIdLong)
            result
                .onSuccess {
                    val index = _postList.value.indexOfFirst { it.id == postId }
                    if (index >= 0) {
                        val p = _postList.value[index]
                        val newList = _postList.value.toMutableList()
                        newList[index] = p.copy(isCollect = !p.isCollect)
                        _postList.value = newList
                    }
                    Log.d(TAG, "toggleBookmark: postId=$postId success")
                }
                .onFailure { Log.e(TAG, "toggleBookmark: postId=$postId failed", it) }
        }
    }

    companion object {
        private const val TAG = "HistoryViewModel"
    }
}
