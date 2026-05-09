package com.vortexa.ui.page.home.pager.home.communicate

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.config.UserConfig
import com.vortexa.model.Post
import com.vortexa.model.PostItem
import com.vortexa.model.TeacherItem
import com.vortexa.repository.HomeRepository
import com.vortexa.repository.UserRepository
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.page.home.pager.profile.ProfileSyncCenter
import com.vortexa.ui.page.post.detail.PostDetailSyncCenter
import com.vortexa.ui.page.post.detail.applyPostDetailSync
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * 交流页 ViewModel。
 * 负责帖子分区筛选、分页列表（上拉加载更多）、推荐导师（与推荐页相同的热门导师接口）及点赞/收藏状态。
 */
class CommunicateViewModel : ViewModel() {

    /** 交流帖分页大小，与接口 pageSize 一致 */
    private val postPageSize = 4

    /** 当前已加载到的帖子页码（首屏成功后为 1） */
    private var discussionPageNum = 1

    /** 接口返回的帖子总数，用于判断是否还有更多 */
    private var discussionTotalCount = 0

    /** 当前选中的帖子分区：1 综合，2 杂谈，3 交易经验，4 玩法 */
    private val _selectedPostType = MutableStateFlow(1)
    val selectedPostType: StateFlow<Int> = _selectedPostType.asStateFlow()

    private val _postList = MutableStateFlow<List<Post>>(emptyList())
    val postList: StateFlow<List<Post>> = _postList.asStateFlow()

    private val _pageStatus = MutableStateFlow<PageStatus>(PageStatus.Loading)
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    /** 横向导师推荐区数据，来自 [HomeRepository.getRecommendTeachers]，与 [com.vortexa.ui.page.home.pager.home.recommend.RecommendViewModel] 热门导师同源 */
    private val _tutorRecommendList = MutableStateFlow<List<TeacherItem>>(emptyList())
    val tutorRecommendList: StateFlow<List<TeacherItem>> = _tutorRecommendList.asStateFlow()

    private val _hasMorePosts = MutableStateFlow(false)
    /** 是否还能加载更多交流帖（已加载条数小于接口 total） */
    val hasMorePosts: StateFlow<Boolean> = _hasMorePosts.asStateFlow()

    private val _loadingMorePosts = MutableStateFlow(false)
    /** 正在请求下一页交流帖时为 true，防止重复触发 */
    val loadingMorePosts: StateFlow<Boolean> = _loadingMorePosts.asStateFlow()

    /**
     * 用户是否曾将交流列表滚离过顶部。为 true 时切回交流 tab 不再自动置顶，保留阅读位置。
     */
    var communicateListUserScrolled: Boolean = false
        private set

    fun markCommunicateListUserScrolled() {
        communicateListUserScrolled = true
    }

    private val homeRepository by lazy { HomeRepository() }
    private val userRepository by lazy { UserRepository() }

    init {
        observePostDetailSync()
        observeProfileSync()
        loadPosts(_selectedPostType.value)
    }

    /**
     * 监听帖子详情页的变更事件，并将最新状态合并到交流列表。
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

    private fun observeProfileSync() {
        viewModelScope.launch {
            ProfileSyncCenter.events.collect {
                Log.d(TAG, "observeProfileSync: refresh")
                refresh(showRefreshing = false)
            }
        }
    }

    /**
     * 按分区加载交流帖，每页最多 4 条。
     * @param postType 1 综合，2 杂谈，3 交易经验，4 玩法
     * @param showPageLoading 为 true 时进入全屏 Loading（筛选、首次进入）；下拉刷新应传 false，避免遮挡列表。
     */
    fun loadPosts(postType: Int, showPageLoading: Boolean = true) {
        _selectedPostType.value = postType
        viewModelScope.launch {
            if (showPageLoading) {
                _pageStatus.value = PageStatus.Loading
            }
            Log.d(TAG, "loadPosts: postType=$postType, showPageLoading=$showPageLoading")
            runPostsAndTutorsLoad { fetchDiscussionPostsFirstPage(postType) }
        }
    }

    /**
     * 上拉加载更多：请求当前分区下一页帖子并追加到列表；无更多或正在加载时直接返回。
     * @return 无返回值。
     */
    fun loadMorePosts() {
        if (!_hasMorePosts.value || _loadingMorePosts.value) {
            Log.d(
                TAG,
                "loadMorePosts skipped: hasMore=${_hasMorePosts.value}, loading=${_loadingMorePosts.value}"
            )
            return
        }
        if (_pageStatus.value != PageStatus.Success) {
            Log.d(TAG, "loadMorePosts skipped: pageStatus=${_pageStatus.value}")
            return
        }
        viewModelScope.launch {
            _loadingMorePosts.value = true
            Log.i(TAG, "loadMorePosts start, nextPage=${discussionPageNum + 1}")
            try {
                loadDiscussionNextPage(_selectedPostType.value)
            } catch (e: Exception) {
                Log.e(TAG, "loadMorePosts failed", e)
                _hasMorePosts.value = false
            } finally {
                _loadingMorePosts.value = false
                Log.i(TAG, "loadMorePosts end")
            }
        }
    }

    /**
     * 下拉刷新：按当前选中分区重新请求交流帖列表。
     * @param showRefreshing 是否展示 Material 下拉刷新指示器；并发刷新时忽略第二次调用。
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
                runPostsAndTutorsLoad { fetchDiscussionPostsFirstPage(_selectedPostType.value) }
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
     * 请求指定分区第一页交流帖，覆盖当前列表并重置分页状态。
     * @param postType 分区索引，与 [loadPosts] 一致。
     * @return 无返回值。
     */
    private suspend fun fetchDiscussionPostsFirstPage(postType: Int) {
        homeRepository.getDiscussionPosts(pageNum = 1, pageSize = postPageSize, postType = postType)
            .onSuccess { response ->
                discussionTotalCount = response.total
                discussionPageNum = 1
                val posts = mapPostItems(response.list)
                _postList.value = posts
                _pageStatus.value =
                    if (posts.isEmpty()) PageStatus.Empty else PageStatus.Success
                _hasMorePosts.value = posts.isNotEmpty() && posts.size < discussionTotalCount
                Log.d(
                    TAG,
                    "fetchDiscussionPostsFirstPage: success, postType=$postType, size=${posts.size}, total=$discussionTotalCount, hasMore=${_hasMorePosts.value}"
                )
            }
            .onFailure {
                _pageStatus.value = PageStatus.Fail
                _hasMorePosts.value = false
                Log.e(TAG, "fetchDiscussionPostsFirstPage: failed, postType=$postType", it)
            }
    }

    /**
     * 请求当前分区下一页并追加到 [postList]。
     * @param postType 当前选中的分区
     * @return 无返回值。
     */
    private suspend fun loadDiscussionNextPage(postType: Int) {
        val nextPage = discussionPageNum + 1
        homeRepository.getDiscussionPosts(pageNum = nextPage, pageSize = postPageSize, postType = postType)
            .onSuccess { response ->
                discussionTotalCount = response.total
                val newPosts = mapPostItems(response.list)
                if (newPosts.isEmpty()) {
                    _hasMorePosts.value = false
                    Log.d(TAG, "loadDiscussionNextPage: empty page, postType=$postType")
                    return@onSuccess
                }
                val merged = _postList.value + newPosts
                _postList.value = merged
                discussionPageNum = nextPage
                _hasMorePosts.value = merged.size < discussionTotalCount
                Log.d(
                    TAG,
                    "loadDiscussionNextPage: success, postType=$postType, added=${newPosts.size}, merged=${merged.size}, hasMore=${_hasMorePosts.value}"
                )
            }
            .onFailure {
                Log.e(TAG, "loadDiscussionNextPage: failed, postType=$postType", it)
                _hasMorePosts.value = false
            }
    }

    /**
     * 将接口帖子项映射为列表用 [Post]。
     * @param items 接口返回的帖子列表
     * @return 映射后的 [Post] 列表
     */
    private fun mapPostItems(items: List<PostItem>): List<Post> = items.map { item ->
        Post(
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
    }

    /**
     * 并行执行帖子加载任务与热门导师接口，与推荐页刷新策略一致。
     * @param postsBlock 当前场景下的帖子拉取逻辑（分区加载或下拉刷新）。
     * @return 无返回值。
     */
    private suspend fun runPostsAndTutorsLoad(postsBlock: suspend () -> Unit) {
        coroutineScope {
            val postsTask = async { postsBlock() }
            val tutorsTask = async { fetchRecommendTeachers() }
            postsTask.await()
            tutorsTask.await()
        }
    }

    /**
     * 调用与推荐页相同的热门导师接口并更新 [tutorRecommendList]。
     * @return 无返回值。
     */
    private suspend fun fetchRecommendTeachers() {
        val userId = UserConfig.getUserId()
        homeRepository.getRecommendTeachers(userId = if (userId > 0) userId else null)
            .onSuccess { response ->
                _tutorRecommendList.value = response.list
                Log.d(TAG, "fetchRecommendTeachers: success, size=${response.list.size}")
            }
            .onFailure {
                Log.e(TAG, "fetchRecommendTeachers: failed", it)
                _tutorRecommendList.value = emptyList()
            }
    }

    /** 切换筛选分区并重新加载 */
    fun selectPostType(postType: Int) {
        if (_selectedPostType.value == postType) return
        loadPosts(postType)
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

    private companion object {
        const val TAG = "CommunicateViewModel"
    }
}
