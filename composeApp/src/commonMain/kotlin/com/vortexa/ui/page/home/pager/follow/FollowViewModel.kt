package com.vortexa.ui.page.home.pager.follow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.vortexa.config.TokenConfig
import com.vortexa.model.FollowedUser
import com.vortexa.model.Post
import com.vortexa.net.auth.isLoginRequired
import com.vortexa.repository.FollowRepository
import com.vortexa.repository.UserRepository
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.page.home.pager.profile.ProfileSyncCenter
import com.vortexa.ui.page.post.detail.PostDetailSyncCenter
import com.vortexa.ui.page.post.detail.applyPostDetailSync
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * 关注页 ViewModel。
 * 负责已关注用户列表（GET /v/api/dynamic/followingList）、关注流帖子列表及点赞/收藏状态。
 */
class FollowViewModel : ViewModel() {

    private val userRepository by lazy { UserRepository() }
    private val followRepository by lazy { FollowRepository() }

    private val _followingList = MutableStateFlow<List<FollowedUser>>(emptyList())
    val followingList: StateFlow<List<FollowedUser>> = _followingList.asStateFlow()

    private val _postList = MutableStateFlow<List<Post>>(emptyList())
    val postList: StateFlow<List<Post>> = _postList.asStateFlow()

    private val _selectedFollowingUserId = MutableStateFlow<Long?>(null)
    val selectedFollowingUserId: StateFlow<Long?> = _selectedFollowingUserId.asStateFlow()

    /**
     * 访客未点进「关注」Tab 前为 Success，避免首屏 `/v/api/dynamic/` 触发登录拦截。
     */
    private val _pageStatus = MutableStateFlow(
        if (TokenConfig.getToken().isNotEmpty()) PageStatus.Loading else PageStatus.Success
    )
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    /** 未登录时仅在为 true 后才允许 [loadAllLists]（由 [FollowView] 首次选中 Tab 时置位） */
    private var guestFollowTabActivated = false

    private val followPostPageSize = 20
    private var followDynamicPageNum = 1
    private var followDynamicTotal = 0

    private val _hasMorePosts = MutableStateFlow(false)
    val hasMorePosts: StateFlow<Boolean> = _hasMorePosts.asStateFlow()

    private val _loadingMorePosts = MutableStateFlow(false)
    val loadingMorePosts: StateFlow<Boolean> = _loadingMorePosts.asStateFlow()

    init {
        observePostDetailSync()
        observeProfileSync()
        if (TokenConfig.getToken().isNotEmpty()) {
            loadAllLists(showPageLoading = true)
        }
    }

    /** 未登录用户首次进入关注 Tab 时调用，否则详情页同步等逻辑不会误拉关注接口 */
    fun activateGuestFollowTab() {
        guestFollowTabActivated = true
    }

    /**
     * 监听帖子详情页的变更事件。
     * 帖子交互直接合并到列表；关注关系变化时重新拉取关注页数据。
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
                if (event.authorId != null && event.isFollowed != null) {
                    Log.i(TAG, "observePostDetailSync: follow changed, authorId=${event.authorId}")
                    loadAllLists(showPageLoading = false)
                }
            }
        }
    }

    private fun observeProfileSync() {
        viewModelScope.launch {
            ProfileSyncCenter.events.collect {
                Log.d(TAG, "observeProfileSync: reload lists")
                loadAllLists(showPageLoading = false)
            }
        }
    }

    /**
     * 并发刷新关注用户列表与关注流帖子；任一接口失败则页面进入 [PageStatus.Fail]；
     * 均成功但已关注用户列表为空时进入 [PageStatus.Empty]（无横向头像数据）。
     *
     * @param showPageLoading 为 true 时展示整页 Loading（首次进入或错误页点击刷新）。
     */
    fun loadAllLists(showPageLoading: Boolean = true) {
        viewModelScope.launch {
            if (TokenConfig.getToken().isEmpty() && !guestFollowTabActivated) {
                Log.d(TAG, "loadAllLists skipped: guest, 关注 Tab 未展示过")
                return@launch
            }
            if (showPageLoading) {
                _pageStatus.value = PageStatus.Loading
            }
            var followingOk = false
            var postsOk = false
            try {
                coroutineScope {
                    val followingTask = async { loadFollowingListSuspend() }
                    val postsTask = async { loadPostsFirstPageSuspend() }
                    followingOk = followingTask.await()
                    postsOk = postsTask.await()
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadAllLists failed", e)
            }
            _pageStatus.value = when {
                !followingOk || !postsOk -> PageStatus.Fail
                _followingList.value.isEmpty() -> PageStatus.Empty
                else -> PageStatus.Success
            }
        }
    }

    private suspend fun loadFollowingListSuspend(): Boolean {
        return followRepository.getFollowingList(pageNum = 1, pageSize = 20)
            .fold(
                onSuccess = { response ->
                    val users = response.list.map { followRepository.mapToFollowedUser(it) }
                    _followingList.value = users
                    val selectedUserId = _selectedFollowingUserId.value
                    if (selectedUserId != null && users.none { it.userId == selectedUserId }) {
                        _selectedFollowingUserId.value = null
                    }
                    Log.d(TAG, "loadFollowingList: success, size=${_followingList.value.size}")
                    true
                },
                onFailure = {
                    if (it.isLoginRequired()) {
                        _followingList.value = emptyList()
                        Log.i(TAG, "loadFollowingList: guest, empty")
                        return@fold true
                    }
                    Log.e(TAG, "loadFollowingList: failed", it)
                    _followingList.value = emptyList()
                    false
                }
            )
    }

    private suspend fun loadPostsFirstPageSuspend(): Boolean {
        val followingId = _selectedFollowingUserId.value
        return followRepository.getDynamicPosts(
            pageNum = 1,
            pageSize = followPostPageSize,
            followingId = followingId
        )
            .fold(
                onSuccess = { response ->
                    followDynamicPageNum = 1
                    followDynamicTotal = response.total
                    val posts = response.list.map { followRepository.mapDynamicItemToPost(it) }
                    _postList.value = posts
                    _hasMorePosts.value = posts.isNotEmpty() && posts.size < followDynamicTotal
                    Log.d(
                        TAG,
                        "loadPostsFirstPage: success, size=${posts.size}, total=$followDynamicTotal, hasMore=${_hasMorePosts.value}"
                    )
                    true
                },
                onFailure = {
                    if (it.isLoginRequired()) {
                        _postList.value = emptyList()
                        _hasMorePosts.value = false
                        Log.i(TAG, "loadPostsFirstPage: guest, empty")
                        return@fold true
                    }
                    Log.e(TAG, "loadPostsFirstPage: failed", it)
                    _postList.value = emptyList()
                    _hasMorePosts.value = false
                    false
                }
            )
    }

    /**
     * 关注流动态列表接近底部时加载下一页并追加；无更多或首屏未成功时不请求。
     */
    fun loadMorePosts() {
        if (!_hasMorePosts.value || _loadingMorePosts.value) return
        if (_pageStatus.value != PageStatus.Success) return
        viewModelScope.launch {
            _loadingMorePosts.value = true
            try {
                loadDynamicNextPageSuspend(_selectedFollowingUserId.value)
            } catch (e: Exception) {
                Log.e(TAG, "loadMorePosts failed", e)
                _hasMorePosts.value = false
            } finally {
                _loadingMorePosts.value = false
            }
        }
    }

    private suspend fun loadDynamicNextPageSuspend(followingId: Long?) {
        val nextPage = followDynamicPageNum + 1
        followRepository.getDynamicPosts(
            pageNum = nextPage,
            pageSize = followPostPageSize,
            followingId = followingId
        )
            .onSuccess { response ->
                followDynamicTotal = response.total
                val newPosts = response.list.map { followRepository.mapDynamicItemToPost(it) }
                if (newPosts.isEmpty()) {
                    _hasMorePosts.value = false
                    Log.d(TAG, "loadDynamicNextPage: empty page")
                    return@onSuccess
                }
                val merged = _postList.value + newPosts
                _postList.value = merged
                followDynamicPageNum = nextPage
                _hasMorePosts.value = merged.size < followDynamicTotal
                Log.d(
                    TAG,
                    "loadDynamicNextPage: added=${newPosts.size}, merged=${merged.size}, hasMore=${_hasMorePosts.value}"
                )
            }
            .onFailure {
                Log.e(TAG, "loadDynamicNextPage: failed", it)
                _hasMorePosts.value = false
            }
    }

    /** Activity/Fragment [androidx.lifecycle.Lifecycle.Event.ON_RESUME] 时静默刷新关注人与动态首屏 */
    fun refreshOnResume() {
        loadAllLists(showPageLoading = false)
    }

    /** 选择关注用户筛选动态；再次点击同一用户恢复全部关注流。 */
    fun selectFollowingUser(userId: Long) {
        val nextUserId = if (_selectedFollowingUserId.value == userId) null else userId
        _selectedFollowingUserId.value = nextUserId
        viewModelScope.launch {
            _pageStatus.value = PageStatus.Loading
            val postsOk = loadPostsFirstPageSuspend()
            _pageStatus.value = when {
                !postsOk -> PageStatus.Fail
                _followingList.value.isEmpty() -> PageStatus.Empty
                else -> PageStatus.Success
            }
        }
    }

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
                        newList[idx] = p.copy(isCollect = !p.isCollect)
                        _postList.value = newList
                    }
                    Log.d(TAG, "toggleBookmark: postId=$postId success")
                }
                .onFailure { Log.e(TAG, "toggleBookmark: postId=$postId failed", it) }
        }
    }

    companion object {
        private const val TAG = "FollowViewModel"
    }
}
