package com.vortexa.ui.page.profile.other

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.model.Post
import com.vortexa.model.UserCenterCommentItem
import com.vortexa.model.UserProfileResponse
import com.vortexa.model.toPost
import com.vortexa.repository.UserRepository
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.page.post.detail.PostDetailSyncCenter
import com.vortexa.ui.page.post.detail.applyPostDetailSync
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val POSTS_PAGE_SIZE = 5
private const val COMMENTS_PAGE_SIZE = 10

/**
 * 他人个人主页。进入时请求 `GET /v/api/user/profile/{userId}`，并支持关注/取消关注、发帖/回复分页列表。
 */
class OtherUserProfileViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _pageStatus = MutableStateFlow<PageStatus>(PageStatus.Loading)
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    private val _profile = MutableStateFlow<UserProfileResponse?>(null)
    val profile: StateFlow<UserProfileResponse?> = _profile.asStateFlow()

    private val _followLoading = MutableStateFlow(false)
    val followLoading: StateFlow<Boolean> = _followLoading.asStateFlow()

    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts: StateFlow<List<Post>> = _userPosts.asStateFlow()

    private val _postsPageStatus = MutableStateFlow<PageStatus>(PageStatus.Loading)
    val postsPageStatus: StateFlow<PageStatus> = _postsPageStatus.asStateFlow()

    private val _postsLoadingMore = MutableStateFlow(false)
    val postsLoadingMore: StateFlow<Boolean> = _postsLoadingMore.asStateFlow()

    private val _postsHasMore = MutableStateFlow(true)
    val postsHasMore: StateFlow<Boolean> = _postsHasMore.asStateFlow()

    private val _userComments = MutableStateFlow<List<UserCenterCommentItem>>(emptyList())
    val userComments: StateFlow<List<UserCenterCommentItem>> = _userComments.asStateFlow()

    private val _commentsPageStatus = MutableStateFlow<PageStatus>(PageStatus.Loading)
    val commentsPageStatus: StateFlow<PageStatus> = _commentsPageStatus.asStateFlow()

    private val _commentsLoadingMore = MutableStateFlow(false)
    val commentsLoadingMore: StateFlow<Boolean> = _commentsLoadingMore.asStateFlow()

    private val _commentsHasMore = MutableStateFlow(true)
    val commentsHasMore: StateFlow<Boolean> = _commentsHasMore.asStateFlow()

    private var targetUserId: Long = -1L
    private var postsPageNum: Int = 1
    private var commentsPageNum: Int = 1
    private var commentsLoadedForUser: Long = -1L

    init {
        observePostDetailSync()
    }

    private fun observePostDetailSync() {
        viewModelScope.launch {
            PostDetailSyncCenter.events.collect { event ->
                val current = _userPosts.value
                val updated = current.applyPostDetailSync(event)
                if (updated !== current) {
                    _userPosts.value = updated
                    Log.d(TAG, "observePostDetailSync: merged postId=${event.postId}")
                }
            }
        }
    }

    fun loadProfile(userId: Long) {
        if (userId <= 0L) {
            Log.w(TAG, "loadProfile: invalid userId=$userId")
            _pageStatus.value = PageStatus.Fail
            return
        }
        targetUserId = userId
        viewModelScope.launch {
            _pageStatus.value = PageStatus.Loading
            Log.d(TAG, "loadProfile: userId=$userId")
            userRepository.getUserProfile(userId)
                .onSuccess { data ->
                    _profile.value = data
                    _pageStatus.value = PageStatus.Success
                    Log.d(TAG, "loadProfile: success")
                }
                .onFailure {
                    _pageStatus.value = PageStatus.Fail
                    Log.e(TAG, "loadProfile: failed", it)
                }
        }
    }

    fun refreshProfile() {
        val uid = targetUserId
        if (uid > 0L) loadProfile(uid)
    }

    /** [userId] 变化时重置发帖/回复列表并重新拉取发帖第一页（回复 Tab 首次进入再请求）。 */
    fun onFeedUserIdChanged(userId: Long) {
        if (userId <= 0L) return
        commentsLoadedForUser = -1L
        _userComments.value = emptyList()
        _commentsPageStatus.value = PageStatus.Loading
        loadUserPostsFirstPage(userId)
    }

    /** 进入页面或切换用户时加载发帖第一页（覆盖）。 */
    fun loadUserPostsFirstPage(userId: Long) {
        if (userId <= 0L) return
        targetUserId = userId
        postsPageNum = 1
        viewModelScope.launch {
            _postsPageStatus.value = PageStatus.Loading
            userRepository.getUserCenterPosts(userId, pageNum = 1, pageSize = POSTS_PAGE_SIZE)
                .onSuccess { resp ->
                    val posts = resp.list.map { it.toPost() }
                    _userPosts.value = posts
                    postsPageNum = 1
                    _postsHasMore.value = posts.size < resp.total
                    _postsPageStatus.value =
                        if (posts.isEmpty()) PageStatus.Empty else PageStatus.Success
                    Log.d(TAG, "loadUserPostsFirstPage: size=${posts.size} total=${resp.total}")
                }
                .onFailure {
                    _userPosts.value = emptyList()
                    _postsPageStatus.value = PageStatus.Fail
                    Log.e(TAG, "loadUserPostsFirstPage failed", it)
                }
        }
    }

    fun loadUserPostsNextPage() {
        val uid = targetUserId
        if (uid <= 0L || postsPageStatus.value != PageStatus.Success) return
        if (_postsLoadingMore.value || !_postsHasMore.value) return
        viewModelScope.launch {
            _postsLoadingMore.value = true
            val next = postsPageNum + 1
            userRepository.getUserCenterPosts(uid, pageNum = next, pageSize = POSTS_PAGE_SIZE)
                .onSuccess { resp ->
                    val newPosts = resp.list.map { it.toPost() }
                    _userPosts.value = _userPosts.value + newPosts
                    postsPageNum = next
                    _postsHasMore.value = _userPosts.value.size < resp.total
                    Log.d(TAG, "loadUserPostsNextPage: added=${newPosts.size}")
                }
                .onFailure {
                    Log.e(TAG, "loadUserPostsNextPage failed", it)
                }
            _postsLoadingMore.value = false
        }
    }

    /** 首次进入「回复」Tab 时加载；同一用户已成功加载过则不重复请求，除非 [force]。 */
    fun loadUserCommentsFirstPage(userId: Long, force: Boolean = false) {
        if (userId <= 0L) return
        if (!force && commentsLoadedForUser == userId) return
        commentsPageNum = 1
        viewModelScope.launch {
            _commentsPageStatus.value = PageStatus.Loading
            userRepository.getUserCenterComments(
                userId,
                pageNum = 1,
                pageSize = COMMENTS_PAGE_SIZE
            )
                .onSuccess { resp ->
                    commentsLoadedForUser = userId
                    _userComments.value = resp.list
                    commentsPageNum = 1
                    _commentsHasMore.value = resp.list.size < resp.total
                    _commentsPageStatus.value =
                        if (resp.list.isEmpty()) PageStatus.Empty else PageStatus.Success
                    Log.d(TAG, "loadUserCommentsFirstPage: size=${resp.list.size}")
                }
                .onFailure {
                    _userComments.value = emptyList()
                    _commentsPageStatus.value = PageStatus.Fail
                    Log.e(TAG, "loadUserCommentsFirstPage failed", it)
                }
        }
    }

    fun loadUserCommentsNextPage() {
        val uid = targetUserId
        if (uid <= 0L || commentsPageStatus.value != PageStatus.Success) return
        if (_commentsLoadingMore.value || !_commentsHasMore.value) return
        viewModelScope.launch {
            _commentsLoadingMore.value = true
            val next = commentsPageNum + 1
            userRepository.getUserCenterComments(uid, pageNum = next, pageSize = COMMENTS_PAGE_SIZE)
                .onSuccess { resp ->
                    _userComments.value = _userComments.value + resp.list
                    commentsPageNum = next
                    _commentsHasMore.value = _userComments.value.size < resp.total
                    Log.d(TAG, "loadUserCommentsNextPage: added=${resp.list.size}")
                }
                .onFailure {
                    Log.e(TAG, "loadUserCommentsNextPage failed", it)
                }
            _commentsLoadingMore.value = false
        }
    }

    fun toggleFollow() {
        val current = _profile.value ?: return
        val uid = current.userInfo.userId
        val followed = current.isFollowed
        viewModelScope.launch {
            _followLoading.value = true
            val result = if (followed) {
                userRepository.unfollow(uid)
            } else {
                userRepository.follow(uid)
            }
            result.onSuccess {
                _profile.value = current.copy(isFollowed = !followed)
            }.onFailure {
                Log.e(TAG, "toggleFollow failed", it)
            }
            _followLoading.value = false
        }
    }

    fun togglePostLike(postId: String) {
        val postIdLong = postId.toLongOrNull() ?: return
        val list = _userPosts.value
        val index = list.indexOfFirst { it.id == postId }
        if (index == -1) return
        val post = list[index]
        viewModelScope.launch {
            val result =
                if (post.isLiked) userRepository.unlikePost(postIdLong)
                else userRepository.likePost(postIdLong)
            result
                .onSuccess {
                    val idx = _userPosts.value.indexOfFirst { it.id == postId }
                    if (idx != -1) {
                        val p = _userPosts.value[idx]
                        val newList = _userPosts.value.toMutableList()
                        newList[idx] = p.copy(
                            isLiked = !p.isLiked,
                            likeCount = (p.likeCount + if (p.isLiked) -1 else 1).coerceAtLeast(0)
                        )
                        _userPosts.value = newList
                    }
                    Log.d(TAG, "togglePostLike: postId=$postId")
                }
                .onFailure { Log.e(TAG, "togglePostLike failed", it) }
        }
    }

    fun togglePostBookmark(postId: String) {
        val postIdLong = postId.toLongOrNull() ?: return
        val list = _userPosts.value
        val index = list.indexOfFirst { it.id == postId }
        if (index == -1) return
        val post = list[index]
        viewModelScope.launch {
            val result =
                if (post.isCollect) userRepository.uncollectPost(postIdLong)
                else userRepository.collectPost(postIdLong)
            result
                .onSuccess {
                    val idx = _userPosts.value.indexOfFirst { it.id == postId }
                    if (idx != -1) {
                        val p = _userPosts.value[idx]
                        val newList = _userPosts.value.toMutableList()
                        newList[idx] = p.copy(isCollect = !p.isCollect)
                        _userPosts.value = newList
                    }
                    Log.d(TAG, "togglePostBookmark: postId=$postId")
                }
                .onFailure { Log.e(TAG, "togglePostBookmark failed", it) }
        }
    }

    fun toggleCommentLike(commentId: Long) {
        val list = _userComments.value
        val index = list.indexOfFirst { it.commentId == commentId }
        if (index == -1) return
        val item = list[index]
        viewModelScope.launch {
            val result =
                if (item.isLiked) userRepository.unlikeComment(commentId)
                else userRepository.likeComment(commentId)
            result
                .onSuccess {
                    val idx = _userComments.value.indexOfFirst { it.commentId == commentId }
                    if (idx != -1) {
                        val c = _userComments.value[idx]
                        val newList = _userComments.value.toMutableList()
                        newList[idx] = c.copy(
                            isLiked = !c.isLiked,
                            likeCount = (c.likeCount + if (c.isLiked) -1 else 1).coerceAtLeast(0)
                        )
                        _userComments.value = newList
                    }
                }
                .onFailure { Log.e(TAG, "toggleCommentLike failed", it) }
        }
    }

    companion object {
        private const val TAG = "OtherUserProfileVM"
    }
}
