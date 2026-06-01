package com.vortexa.ui.page.home.pager.home.recommend

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.config.UserConfig
import com.vortexa.model.Post
import com.vortexa.model.RecommendCard
import com.vortexa.repository.HomeRepository
import com.vortexa.repository.UserRepository
import com.vortexa.ui.page.home.HomePostCreateSyncCenter
import com.vortexa.ui.page.home.pager.profile.ProfileSyncCenter
import com.vortexa.ui.component.pageStatus.PageStatus
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
 * ViewModel for Recommend page.
 * Handles data loading and user interactions for the post list.
 */
class RecommendViewModel : ViewModel() {

    // Backing property to avoid state updates from other classes
    private val _postList = MutableStateFlow<List<Post>>(emptyList())

    // The UI collects from this StateFlow to get its state updates
    val postList: StateFlow<List<Post>> = _postList.asStateFlow()

    private val _recommendCards = MutableStateFlow<List<RecommendCard>>(emptyList())
    val recommendCards: StateFlow<List<RecommendCard>> = _recommendCards.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _pageStatus = MutableStateFlow(PageStatus.Loading)
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    private val homeRepository by lazy { HomeRepository() }
    private val userRepository by lazy { UserRepository() }

    init {
        observePostDetailSync()
        observeProfileSync()
        observePostCreateSync()
        refresh(showRefreshing = false)
    }

    /**
     * 监听帖子详情页的变更事件，并将最新状态合并到推荐列表。
     * @return 无返回值。
     */
    private fun observePostDetailSync() {
        viewModelScope.launch {
            PostDetailSyncCenter.events.collect { event ->
                val current = _postList.value
                val updated = current.applyPostDetailSync(event)
                if (updated !== current) {
                    _postList.value = updated
                    Log.d("RecommendViewModel", "observePostDetailSync: merged postId=${event.postId}")
                }
            }
        }
    }

    private fun observeProfileSync() {
        viewModelScope.launch {
            ProfileSyncCenter.events.collect {
                Log.d("RecommendViewModel", "observeProfileSync: refresh")
                refresh(showRefreshing = false)
            }
        }
    }

    private fun observePostCreateSync() {
        viewModelScope.launch {
            HomePostCreateSyncCenter.events.collect {
                Log.d("RecommendViewModel", "observePostCreateSync: refresh")
                refresh(showRefreshing = false)
            }
        }
    }

    /**
     * 刷新推荐页数据。
     * @param showRefreshing 是否展示下拉刷新加载态；首次进入页面时可传 false。
     * @return 无返回值。
     */
    fun refresh(showRefreshing: Boolean = true) {
        viewModelScope.launch {
            if (showRefreshing && _isRefreshing.value) {
                Log.w("RecommendViewModel", "refresh ignored: already refreshing")
                return@launch
            }
            if (showRefreshing) {
                _isRefreshing.value = true
            }
            Log.i("RecommendViewModel", "refresh start, showRefreshing=$showRefreshing")
            var postsOk = false
            var cardsOk = false
            try {
                coroutineScope {
                    val postTask = async { loadPosts() }
                    val cardTask = async { loadRecommendCards() }
                    postsOk = postTask.await()
                    cardsOk = cardTask.await()
                }
            } catch (e: Exception) {
                Log.e("RecommendViewModel", "refresh failed", e)
                postsOk = false
                cardsOk = false
            } finally {
                if (showRefreshing) {
                    _isRefreshing.value = false
                }
                _pageStatus.value = if (postsOk && cardsOk) PageStatus.Success else PageStatus.Fail
                Log.i("RecommendViewModel", "refresh end")
            }
        }
    }

    /**
     * 拉取推荐帖子数据并写入状态。
     * @return 无返回值。
     */
    private suspend fun loadPosts(): Boolean {
        return homeRepository.getRecommendPosts()
            .fold(
                onSuccess = { response ->
                    val posts = response.list.map { item ->
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
                    _postList.emit(posts)
                    Log.d("RecommendViewModel", "Loaded ${posts.size} posts from API")
                    true
                },
                onFailure = {
                    Log.e("RecommendViewModel", "Failed to load posts", it)
                    _postList.emit(emptyList())
                    false
                }
            )
    }

    /**
     * 拉取推荐导师数据并写入状态。
     * @return 无返回值。
     */
    private suspend fun loadRecommendCards(): Boolean {
        val userId = UserConfig.getUserId()
        return homeRepository.getRecommendTeachers(userId = if (userId > 0) userId else null)
            .fold(
                onSuccess = { response ->
                    val cards = response.list.map { item ->
                        RecommendCard(
                            id = item.teacherId,
                            title = item.nickname,
                            tags = item.tags ?: emptyList(),
                            price = item.price,
                            unit = "积分", // 接口说明单位是元，这里使用 CNY
                            favorite = item.score.toFloatOrNull() ?: 0f,
                            imageUrl = item.avatar
                        )
                    }
                    _recommendCards.emit(cards)
                    Log.d("RecommendViewModel", "Loaded ${cards.size} recommend cards from API")
                    true
                },
                onFailure = {
                    Log.e("RecommendViewModel", "Failed to load recommend cards", it)
                    _recommendCards.emit(emptyList())
                    false
                }
            )
    }

    /**
     * 切换帖子点赞状态，调用点赞/取消点赞接口。
     * @param postId 帖子 ID
     */
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
                    Log.d("RecommendViewModel", "toggleLike: postId=$postId success")
                }
                .onFailure {
                    Log.e("RecommendViewModel", "toggleLike: postId=$postId failed", it)
                }
        }
    }

    /**
     * 切换收藏状态，调用收藏/取消收藏接口。
     * @param postId 帖子 ID
     */
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
                    Log.d("RecommendViewModel", "toggleBookmark: postId=$postId success")
                }
                .onFailure { Log.e("RecommendViewModel", "toggleBookmark: postId=$postId failed", it) }
        }
    }
}
