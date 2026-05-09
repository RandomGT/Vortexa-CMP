package com.vortexa.ui.page.profile.collection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.model.Post
import com.vortexa.repository.UserRepository
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.page.post.detail.PostDetailSyncCenter
import com.vortexa.ui.page.post.detail.applyPostDetailSync
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * 我的收藏页 ViewModel。
 * 负责收藏列表加载（POST /v/api/user/collections）、筛选及点赞/取消收藏交互。
 *
 * @author LuXin
 */
class CollectionViewModel(
    private val repository: CollectionRepository = CollectionRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _postList = MutableStateFlow<List<Post>>(emptyList())
    /** 当前展示的帖子列表（由接口按 module 筛选返回） */
    val postList: StateFlow<List<Post>> = _postList.asStateFlow()

    /** 当前选中的分区 Chip 索引：0=全部，1=杂谈，2=交易经验，3=玩法（请求时转为中文 module 字符串） */
    private val _selectedFilterIndex = MutableStateFlow(0)
    val selectedFilterIndex: StateFlow<Int> = _selectedFilterIndex.asStateFlow()

    private val _pageStatus = MutableStateFlow<PageStatus>(PageStatus.Loading)
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    private var collectionPageNum = 1

    private val _hasMoreCollections = MutableStateFlow(false)
    val hasMoreCollections: StateFlow<Boolean> = _hasMoreCollections.asStateFlow()

    private val _loadingMoreCollections = MutableStateFlow(false)
    val loadingMoreCollections: StateFlow<Boolean> = _loadingMoreCollections.asStateFlow()

    init {
        observePostDetailSync()
        loadCollection()
    }

    /**
     * 监听帖子详情页的变更事件。
     * 收藏页中取消收藏后应直接移除对应帖子，其余状态则局部刷新。
     * @return 无返回值。
     */
    private fun observePostDetailSync() {
        viewModelScope.launch {
            PostDetailSyncCenter.events.collect { event ->
                val current = _postList.value
                val removedList = if (event.isCollect == false) {
                    current.filterNot { it.id == event.postId }
                } else {
                    current
                }
                if (removedList !== current && removedList.size != current.size) {
                    _postList.value = removedList
                    _pageStatus.value = if (removedList.isEmpty()) PageStatus.Empty else PageStatus.Success
                    Log.d(TAG, "observePostDetailSync: removed postId=${event.postId}")
                    return@collect
                }
                val updated = current.applyPostDetailSync(event)
                if (updated !== current) {
                    _postList.value = updated
                    Log.d(TAG, "observePostDetailSync: merged postId=${event.postId}")
                }
            }
        }
    }

    /**
     * 加载收藏列表。
     * 按当前选中的 module 请求接口。
     */
    fun loadCollection() {
        viewModelScope.launch {
            _pageStatus.value = PageStatus.Loading
            _loadingMoreCollections.value = false
            collectionPageNum = 1
            val filterIndex = _selectedFilterIndex.value
            val module = CollectionRepository.moduleParamForFilterIndex(filterIndex)
            Log.d(TAG, "loadCollection: filterIndex=$filterIndex module=$module")
            repository.getCollections(
                module = module,
                pageNum = 1,
                pageSize = PAGE_SIZE
            )
                .onSuccess { response ->
                    val posts = response.list.map { repository.mapToPost(it) }
                    _postList.value = posts
                    collectionPageNum = 1
                    _hasMoreCollections.value = computeHasMore(posts.size, response.total, response.list.size)
                    _pageStatus.value = if (posts.isEmpty()) PageStatus.Empty else PageStatus.Success
                    Log.d(TAG, "loadCollection: success, size=${posts.size}, total=${response.total}")
                }
                .onFailure {
                    _pageStatus.value = PageStatus.Fail
                    _postList.value = emptyList()
                    _hasMoreCollections.value = false
                    Log.e(TAG, "loadCollection: failed", it)
                }
        }
    }

    /**
     * 上拉加载更多：请求下一页并追加；无更多或首页未成功时直接返回。
     */
    fun loadMoreCollections() {
        if (!_hasMoreCollections.value || _loadingMoreCollections.value) return
        if (_pageStatus.value != PageStatus.Success) return
        viewModelScope.launch {
            _loadingMoreCollections.value = true
            val filterIndex = _selectedFilterIndex.value
            val module = CollectionRepository.moduleParamForFilterIndex(filterIndex)
            val nextPage = collectionPageNum + 1
            Log.d(TAG, "loadMoreCollections: filterIndex=$filterIndex module=$module, page=$nextPage")
            try {
                repository.getCollections(
                    module = module,
                    pageNum = nextPage,
                    pageSize = PAGE_SIZE
                )
                    .onSuccess { response ->
                        val newPosts = response.list.map { repository.mapToPost(it) }
                        if (newPosts.isEmpty()) {
                            _hasMoreCollections.value = false
                            Log.d(TAG, "loadMoreCollections: empty page")
                            return@onSuccess
                        }
                        val merged = _postList.value + newPosts
                        _postList.value = merged
                        collectionPageNum = nextPage
                        _hasMoreCollections.value = computeHasMore(merged.size, response.total, newPosts.size)
                        Log.d(
                            TAG,
                            "loadMoreCollections: added=${newPosts.size}, merged=${merged.size}, hasMore=${_hasMoreCollections.value}"
                        )
                    }
                    .onFailure {
                        Log.e(TAG, "loadMoreCollections: failed", it)
                    }
            } finally {
                _loadingMoreCollections.value = false
            }
        }
    }

    /** 当前页有数据且满页，且（若有 total）未达总数时认为还有更多。 */
    private fun computeHasMore(loadedCount: Int, totalFromApi: Int, lastPageSize: Int): Boolean {
        if (lastPageSize == 0) return false
        if (lastPageSize < PAGE_SIZE) return false
        return totalFromApi <= 0 || loadedCount < totalFromApi
    }

    /**
     * 设置筛选分区并重新加载。
     * @param index 0=全部，1=杂谈，2=交易经验，3=玩法
     */
    fun setFilter(index: Int) {
        if (_selectedFilterIndex.value == index) return
        _selectedFilterIndex.value = index
        loadCollection()
    }

    /**
     * 切换点赞状态，调用点赞/取消点赞接口。
     * @param postId 帖子 id
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
     * 取消收藏，调用取消收藏接口，成功后从列表中移除。
     * 收藏页中所有项均为已收藏状态，点击即取消收藏。
     * @param postId 帖子 id
     */
    fun toggleBookmark(postId: String) {
        val postIdLong = postId.toLongOrNull() ?: return
        val list = _postList.value.toMutableList()
        val idx = list.indexOfFirst { it.id == postId }
        if (idx < 0) return
        viewModelScope.launch {
            userRepository.uncollectPost(postIdLong)
                .onSuccess {
                    val index = _postList.value.indexOfFirst { it.id == postId }
                    if (index >= 0) {
                        val newList = _postList.value.toMutableList()
                        newList.removeAt(index)
                        _postList.value = newList
                        if (newList.isEmpty()) _pageStatus.value = PageStatus.Empty
                    }
                    Log.d(TAG, "toggleBookmark: postId=$postId uncollect success")
                }
                .onFailure { Log.e(TAG, "toggleBookmark: postId=$postId failed", it) }
        }
    }

    companion object {
        private const val TAG = "CollectionViewModel"
        private const val PAGE_SIZE = 20
    }
}
