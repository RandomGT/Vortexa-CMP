package com.vortexa.ui.page.post.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.model.Post
import com.vortexa.model.PostItem
import com.vortexa.repository.HomeRepository
import com.vortexa.repository.UserRepository
import com.vortexa.ui.page.post.detail.PostDetailSyncCenter
import com.vortexa.ui.page.post.detail.applyPostDetailSync
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** 每页条数 */
private const val PAGE_SIZE = 10

/**
 * 热帖列表页 ViewModel。
 * 与 Recommend 使用同一接口 [HomeRepository.getRecommendPosts]，每页 [PAGE_SIZE] 条，支持分页加载。
 */
class HotPostListViewModel : ViewModel() {

    private val _postList = MutableStateFlow<List<Post>>(emptyList())
    val postList: StateFlow<List<Post>> = _postList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private var currentPage = 1
    private val homeRepository by lazy { HomeRepository() }
    private val userRepository by lazy { UserRepository() }

    init {
        observePostDetailSync()
        loadFirstPage()
    }

    /**
     * 监听帖子详情页的变更事件，并将最新状态合并到热帖列表。
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

    /** 加载第一页，覆盖当前列表 */
    fun loadFirstPage() {
        viewModelScope.launch {
            currentPage = 1
            _isLoading.value = true
            Log.d(TAG, "Loading first page, pageSize=$PAGE_SIZE")
            homeRepository.getRecommendPosts(pageNum = 1, pageSize = PAGE_SIZE)
                .onSuccess { response ->
                    val posts = response.list.map { item -> mapItemToPost(item) }
                    _postList.value = posts
                    _hasMore.value = (response.list.size >= PAGE_SIZE)
                    Log.d(TAG, "Loaded ${posts.size} posts")
                }
                .onFailure {
                    Log.e(TAG, "Failed to load posts", it)
                    _postList.value = emptyList()
                }
            _isLoading.value = false
        }
    }

    /** 加载下一页，追加到列表末尾 */
    fun loadNextPage() {
        if (_isLoading.value || !_hasMore.value) return
        viewModelScope.launch {
            _isLoading.value = true
            val nextPage = currentPage + 1
            Log.d(TAG, "Loading page $nextPage, pageSize=$PAGE_SIZE")
            homeRepository.getRecommendPosts(pageNum = nextPage, pageSize = PAGE_SIZE)
                .onSuccess { response ->
                    val newPosts = response.list.map { item -> mapItemToPost(item) }
                    _postList.value = _postList.value + newPosts
                    currentPage = nextPage
                    _hasMore.value = (response.list.size >= PAGE_SIZE)
                    Log.d(TAG, "Loaded next page, total=${_postList.value.size}")
                }
                .onFailure {
                    Log.e(TAG, "Failed to load next page", it)
                }
            _isLoading.value = false
        }
    }

    private fun mapItemToPost(item: PostItem): Post = Post(
        id = item.postId.toString(),
        username = item.nickname,
        avatar = item.avatar,
        time = item.publishTime ?: "",
        content = item.summary ?: "",
        images = item.mediaList ?: emptyList(),
        tagName = item.module,
        likeCount = item.likeCount,
        commentCount = item.replyCount,
        isLiked = item.isLiked,
        isCollect = item.isCollect,
        userId = item.userId,
        title = item.title,
        summary = item.summary,
        totalMediaCount = item.totalMediaCount,
        module = item.module,
        isInteractionHot = item.isInteractionHot,
        isViewHot = item.isViewHot,
        collectCount = item.collectCount,
        publishTime = item.publishTime
    )

    /** 切换点赞状态，调用点赞/取消点赞接口 */
    fun toggleLike(postId: String) {
        val postIdLong = postId.toLongOrNull() ?: return
        val list = _postList.value.toMutableList()
        val index = list.indexOfFirst { it.id == postId }
        if (index == -1) return
        val post = list[index]
        viewModelScope.launch {
            val result = if (post.isLiked) userRepository.unlikePost(postIdLong) else userRepository.likePost(postIdLong)
            result
                .onSuccess {
                    val idx = _postList.value.indexOfFirst { it.id == postId }
                    if (idx != -1) {
                        val p = _postList.value[idx]
                        val newList = _postList.value.toMutableList()
                        newList[idx] = p.copy(
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

    /** 切换收藏状态，调用收藏/取消收藏接口 */
    fun toggleBookmark(postId: String) {
        val postIdLong = postId.toLongOrNull() ?: return
        val list = _postList.value.toMutableList()
        val index = list.indexOfFirst { it.id == postId }
        if (index == -1) return
        val post = list[index]
        viewModelScope.launch {
            val result = if (post.isCollect) userRepository.uncollectPost(postIdLong) else userRepository.collectPost(postIdLong)
            result
                .onSuccess {
                    val idx = _postList.value.indexOfFirst { it.id == postId }
                    if (idx != -1) {
                        val p = _postList.value[idx]
                        val newList = _postList.value.toMutableList()
                        newList[idx] = p.copy(
                            isCollect = !p.isCollect,
                            collectCount = (p.collectCount + if (p.isCollect) -1 else 1).coerceAtLeast(0)
                        )
                        _postList.value = newList
                    }
                    Log.d(TAG, "toggleBookmark: postId=$postId success")
                }
                .onFailure { Log.e(TAG, "toggleBookmark: postId=$postId failed", it) }
        }
    }

    companion object {
        private const val TAG = "HotPostListViewModel"
    }
}
