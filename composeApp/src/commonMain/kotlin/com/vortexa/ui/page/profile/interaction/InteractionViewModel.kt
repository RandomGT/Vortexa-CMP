package com.vortexa.ui.page.profile.interaction

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.model.InteractionListItem
import com.vortexa.model.InteractionResponse
import com.vortexa.repository.InteractionRepository
import com.vortexa.ui.component.pageStatus.PageStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 互动管理页 ViewModel。
 * 负责 actorType（所有人/我的关注）、actionType（回复/点赞）、direction（全部/我发起的/被互动的）
 * 筛选参数及列表加载。
 *
 * @author LuXin
 */
class InteractionViewModel(
    private val interactionRepository: InteractionRepository = InteractionRepository()
) : ViewModel() {

    /** 互动对象：0=所有人，1=我的关注 */
    private val _actorType = MutableStateFlow(0)
    val actorType: StateFlow<Int> = _actorType.asStateFlow()

    /** 互动类型：0=点赞，1=回复（对应 Tab） */
    private val _actionType = MutableStateFlow(1)
    val actionType: StateFlow<Int> = _actionType.asStateFlow()

    /** 互动方向：0=全部，1=我发起的，2=被互动的 */
    private val _direction = MutableStateFlow(0)
    val direction: StateFlow<Int> = _direction.asStateFlow()

    /** 回复 Tab 列表（actionType=1），与点赞分页独立，避免 Pager 两页共用一个 List 时 LazyColumn key/状态错乱 */
    private val _replyInteractionList = MutableStateFlow<List<InteractionListItem>>(emptyList())
    val replyInteractionList: StateFlow<List<InteractionListItem>> = _replyInteractionList.asStateFlow()

    private val _likeInteractionList = MutableStateFlow<List<InteractionListItem>>(emptyList())
    val likeInteractionList: StateFlow<List<InteractionListItem>> = _likeInteractionList.asStateFlow()

    private val _replyHasMore = MutableStateFlow(false)
    val replyHasMore: StateFlow<Boolean> = _replyHasMore.asStateFlow()

    private val _likeHasMore = MutableStateFlow(false)
    val likeHasMore: StateFlow<Boolean> = _likeHasMore.asStateFlow()

    private val _replyLoadingMore = MutableStateFlow(false)
    val replyLoadingMore: StateFlow<Boolean> = _replyLoadingMore.asStateFlow()

    private val _likeLoadingMore = MutableStateFlow(false)
    val likeLoadingMore: StateFlow<Boolean> = _likeLoadingMore.asStateFlow()

    private var replyNextPage = 1
    private var likeNextPage = 1

    private var loadJob: Job? = null
    private var loadMoreJob: Job? = null

    private val _pageStatus = MutableStateFlow<PageStatus>(PageStatus.Loading)
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    init {
        loadInteractions()
    }

    /**
     * 设置互动对象并重新加载。
     * @param type 0=所有人，1=我的关注
     */
    fun setActorType(type: Int) {
        if (_actorType.value == type) return
        _actorType.value = type
        loadInteractions()
    }

    /**
     * 设置互动类型（Tab）并重新加载。
     * @param type 0=点赞，1=回复
     */
    fun setActionType(type: Int) {
        if (_actionType.value == type) return
        _actionType.value = type
        loadInteractions()
    }

    /**
     * 设置互动方向并重新加载。
     * @param dir 0=全部，1=我发起的，2=被互动的
     */
    fun setDirection(dir: Int) {
        if (_direction.value == dir) return
        _direction.value = dir
        loadInteractions()
    }

    /** 根据当前筛选参数加载互动列表（首屏 / 筛选变更，从第 1 页开始） */
    fun loadInteractions() {
        loadJob?.cancel()
        loadMoreJob?.cancel()
        loadJob = viewModelScope.launch {
            val actionTypeSnapshot = _actionType.value
            val actorSnapshot = _actorType.value
            val directionSnapshot = _direction.value
            _pageStatus.value = PageStatus.Loading
            if (actionTypeSnapshot == 1) {
                _replyLoadingMore.value = false
            } else {
                _likeLoadingMore.value = false
            }
            Log.d(TAG, "loadInteractions: actorType=$actorSnapshot, actionType=$actionTypeSnapshot, direction=$directionSnapshot")
            interactionRepository.getInteractions(
                actorType = actorSnapshot,
                actionType = actionTypeSnapshot,
                direction = directionSnapshot,
                pageNum = 1,
                pageSize = PAGE_SIZE
            )
                .onSuccess { response ->
                    if (actionTypeSnapshot != _actionType.value ||
                        actorSnapshot != _actorType.value ||
                        directionSnapshot != _direction.value
                    ) {
                        return@onSuccess
                    }
                    if (actionTypeSnapshot == 1) {
                        _replyInteractionList.value = response.list
                        replyNextPage = 2
                        _replyHasMore.value = hasMoreAfterPage(response)
                    } else {
                        _likeInteractionList.value = response.list
                        likeNextPage = 2
                        _likeHasMore.value = hasMoreAfterPage(response)
                    }
                    _pageStatus.value =
                        if (response.list.isEmpty()) PageStatus.Empty else PageStatus.Success
                    Log.d(TAG, "loadInteractions: success, size=${response.list.size}, total=${response.total}")
                }
                .onFailure {
                    if (actionTypeSnapshot != _actionType.value ||
                        actorSnapshot != _actorType.value ||
                        directionSnapshot != _direction.value
                    ) {
                        return@onFailure
                    }
                    _pageStatus.value = PageStatus.Fail
                    if (actionTypeSnapshot == 1) {
                        _replyInteractionList.value = emptyList()
                        _replyHasMore.value = false
                    } else {
                        _likeInteractionList.value = emptyList()
                        _likeHasMore.value = false
                    }
                    Log.e(TAG, "loadInteractions: failed", it)
                }
        }
    }

    /** 回复 Tab 加载下一页 */
    fun loadMoreReplyInteractions() {
        if (!_replyHasMore.value || _replyLoadingMore.value || _replyInteractionList.value.isEmpty()) return
        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch {
            val actorSnapshot = _actorType.value
            val directionSnapshot = _direction.value
            val pageNum = replyNextPage
            _replyLoadingMore.value = true
            try {
                interactionRepository.getInteractions(
                    actorType = actorSnapshot,
                    actionType = 1,
                    direction = directionSnapshot,
                    pageNum = pageNum,
                    pageSize = PAGE_SIZE
                ).onSuccess { response ->
                    if (actorSnapshot != _actorType.value || directionSnapshot != _direction.value) return@onSuccess
                    if (_actionType.value != 1) return@onSuccess
                    val merged = _replyInteractionList.value + response.list
                    _replyInteractionList.value = merged
                    replyNextPage = pageNum + 1
                    _replyHasMore.value = merged.size < response.total && response.list.isNotEmpty()
                }
            } finally {
                _replyLoadingMore.value = false
            }
        }
    }

    /** 点赞 Tab 加载下一页 */
    fun loadMoreLikeInteractions() {
        if (!_likeHasMore.value || _likeLoadingMore.value || _likeInteractionList.value.isEmpty()) return
        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch {
            val actorSnapshot = _actorType.value
            val directionSnapshot = _direction.value
            val pageNum = likeNextPage
            _likeLoadingMore.value = true
            try {
                interactionRepository.getInteractions(
                    actorType = actorSnapshot,
                    actionType = 0,
                    direction = directionSnapshot,
                    pageNum = pageNum,
                    pageSize = PAGE_SIZE
                ).onSuccess { response ->
                    if (actorSnapshot != _actorType.value || directionSnapshot != _direction.value) return@onSuccess
                    if (_actionType.value != 0) return@onSuccess
                    val merged = _likeInteractionList.value + response.list
                    _likeInteractionList.value = merged
                    likeNextPage = pageNum + 1
                    _likeHasMore.value = merged.size < response.total && response.list.isNotEmpty()
                }
            } finally {
                _likeLoadingMore.value = false
            }
        }
    }

    private fun hasMoreAfterPage(response: InteractionResponse): Boolean =
        response.list.size < response.total

    companion object {
        private const val TAG = "InteractionViewModel"
        private const val PAGE_SIZE = 20
    }
}
