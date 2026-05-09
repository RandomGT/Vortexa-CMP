package com.vortexa.ui.page.search.result

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.model.Post
import com.vortexa.repository.SearchRepository
import com.vortexa.repository.UserRepository
import com.vortexa.ui.page.post.detail.PostDetailSyncCenter
import com.vortexa.ui.page.post.detail.applyPostDetailSync
import com.vortexa.ui.component.pageStatus.PageStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * 搜索结果页 ViewModel：Tab 索引 + 帖文列表（来自 /v/api/search/result，仅适配 Post）。
 */
class SearchResultViewModel(
    private val searchRepository: SearchRepository = SearchRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    init {
        observePostDetailSync()
    }

    /** 当前选中的 Tab 索引，对应 [SearchResultTabs] 下标 */
    var selectedTabIndex by mutableStateOf(0)
        private set

    private val _postList = MutableStateFlow<List<Post>>(emptyList())
    /** 帖文 Tab 的帖子列表，供 [PostPage] 消费 */
    val postList: StateFlow<List<Post>> = _postList.asStateFlow()

    private val _postListStatus = MutableStateFlow(PageStatus.Loading)
    /** 帖文搜索结果页状态：加载中 / 成功 / 无结果 / 失败 */
    val postListStatus: StateFlow<PageStatus> = _postListStatus.asStateFlow()

    private var lastSearchKeyword: String = ""
    private var lastSearchTabIndex: Int = 0

    /**
     * 监听帖子详情页的变更事件，并将最新状态合并到搜索结果列表。
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
     * 加载搜索结果，调用 /v/api/search/result。
     * @param keyword 搜索关键词
     * @param tabIndex 当前 Tab 索引，映射为 type（general/post/...）
     */
    fun loadSearchResult(keyword: String, tabIndex: Int = 0) {
        if (keyword.isBlank()) return
        lastSearchKeyword = keyword
        lastSearchTabIndex = tabIndex
        _postListStatus.value = PageStatus.Loading
        val type = SearchResultTypes.getOrElse(tabIndex) { "general" }
        viewModelScope.launch {
            searchRepository.getSearchResult(keyword = keyword, type = type)
                .onSuccess { posts ->
                    _postList.value = posts
                    _postListStatus.value =
                        if (posts.isEmpty()) PageStatus.Empty else PageStatus.Success
                    Log.d(TAG, "Loaded ${posts.size} posts for keyword=$keyword type=$type")
                }
                .onFailure {
                    Log.e(TAG, "Failed to load search result", it)
                    _postList.value = emptyList()
                    _postListStatus.value = PageStatus.Fail
                }
        }
    }

    /**
     * 使用最近一次搜索关键词与 Tab 重新请求（失败态「点击刷新」）。
     */
    fun reloadSearchResult() {
        if (lastSearchKeyword.isNotBlank()) {
            loadSearchResult(lastSearchKeyword, lastSearchTabIndex)
        }
    }

    /**
     * 切换帖子点赞状态（帖文 Tab 内使用），调用点赞/取消点赞接口。
     * @param postId 帖子 id
     */
    fun togglePostLike(postId: String) {
        val postIdLong = postId.toLongOrNull() ?: return
        val current = _postList.value.toMutableList()
        val index = current.indexOfFirst { it.id == postId }
        if (index == -1) return
        val post = current[index]
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
                    Log.d(TAG, "togglePostLike: postId=$postId success")
                }
                .onFailure { Log.e(TAG, "togglePostLike: postId=$postId failed", it) }
        }
    }

    /**
     * 切换帖子收藏状态（帖文 Tab 内使用），调用收藏/取消收藏接口。
     * @param postId 帖子 id
     */
    fun togglePostBookmark(postId: String) {
        val postIdLong = postId.toLongOrNull() ?: return
        val current = _postList.value.toMutableList()
        val index = current.indexOfFirst { it.id == postId }
        if (index == -1) return
        val post = current[index]
        viewModelScope.launch {
            val result = if (post.isCollect) userRepository.uncollectPost(postIdLong) else userRepository.collectPost(postIdLong)
            result
                .onSuccess {
                    val idx = _postList.value.indexOfFirst { it.id == postId }
                    if (idx != -1) {
                        val p = _postList.value[idx]
                        val newList = _postList.value.toMutableList()
                        newList[idx] = p.copy(isCollect = !p.isCollect)
                        _postList.value = newList
                    }
                    Log.d(TAG, "togglePostBookmark: postId=$postId success")
                }
                .onFailure { Log.e(TAG, "togglePostBookmark: postId=$postId failed", it) }
        }
    }

    /**
     * 用户点击 Tab 时调用，用于切换页码。
     * @param index Tab 索引
     */
    fun onTabClick(index: Int) {
        selectedTabIndex = index
    }

    /**
     * 用户滑动 Pager 后同步选中 Tab（由 UI 层在 Pager 滚动结束时调用）。
     * @param index 当前页码
     */
    fun syncTabFromPage(index: Int) {
        selectedTabIndex = index
    }

    private companion object {
        const val TAG = "SearchResultVM"
    }
}
