package com.vortexa.ui.page.creator.statistics

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.model.CreatorData
import com.vortexa.model.CreatorStatisticsPostItem
import com.vortexa.repository.CreatorRepository
import com.vortexa.ui.component.pageStatus.PageStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 数据中心 ViewModel。
 * 管理数据一览、帖子统计列表（含上拉加载更多），支持时间范围与排序切换。
 *
 * @param repository 创作中心仓库，默认 [CreatorRepository]
 */
class DataCenterViewModel(
    private val repository: CreatorRepository = CreatorRepository()
) : ViewModel() {

    private val postPageSize = 20

    private var postPageNum = 1

    private var postTotalCount = 0

    private val _pageStatus = MutableStateFlow<PageStatus>(PageStatus.Loading)
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    private val _creatorData = MutableStateFlow<CreatorData?>(null)
    val creatorData: StateFlow<CreatorData?> = _creatorData.asStateFlow()

    private val _postList = MutableStateFlow<List<CreatorStatisticsPostItem>>(emptyList())
    val postList: StateFlow<List<CreatorStatisticsPostItem>> = _postList.asStateFlow()

    private val _selectedDays = MutableStateFlow(7)
    val selectedDays: StateFlow<Int> = _selectedDays.asStateFlow()

    private val _selectedSortBy = MutableStateFlow(0)
    /** 帖子列表排序：0 默认，1 最多点击，2 最多回复，3 最多点赞，4 最多转发 */
    val selectedSortBy: StateFlow<Int> = _selectedSortBy.asStateFlow()

    private val _hasMorePosts = MutableStateFlow(false)
    val hasMorePosts: StateFlow<Boolean> = _hasMorePosts.asStateFlow()

    private val _loadingMorePosts = MutableStateFlow(false)
    val loadingMorePosts: StateFlow<Boolean> = _loadingMorePosts.asStateFlow()

    init {
        loadAll()
    }

    /**
     * 加载数据一览与帖子列表（首屏/切换天数或排序）。
     *
     * @param days 统计天数，null 时使用当前选中值
     * @param sortBy 排序方式，null 时使用当前选中值
     * @param silent 为 true 时不进入全屏 Loading，失败时保留当前数据（用于从详情返回等场景）。
     */
    fun loadAll(days: Int? = null, sortBy: Int? = null, silent: Boolean = false) {
        val effectiveDays = days ?: _selectedDays.value
        val rawSortBy = sortBy ?: _selectedSortBy.value
        val effectiveSortBy = if (rawSortBy == 5) 0 else rawSortBy
        if (rawSortBy == 5) _selectedSortBy.value = 0
        viewModelScope.launch {
            _loadingMorePosts.value = false
            if (!silent) {
                _pageStatus.value = PageStatus.Loading
            }
            Log.d(TAG, "loadAll: days=$effectiveDays, sortBy=$effectiveSortBy, silent=$silent")

            val dataResult = repository.getCreatorData(effectiveDays)
            val postsResult = repository.getPostDataList(
                days = effectiveDays,
                pageNum = 1,
                pageSize = postPageSize,
                sortBy = effectiveSortBy
            )

            when {
                dataResult.isFailure -> {
                    Log.e(TAG, "loadAll: getCreatorData failed", dataResult.exceptionOrNull())
                    if (!silent) {
                        _pageStatus.value = PageStatus.Fail
                        _hasMorePosts.value = false
                    }
                    return@launch
                }
                postsResult.isFailure -> {
                    Log.e(TAG, "loadAll: getPostDataList failed", postsResult.exceptionOrNull())
                    if (!silent) {
                        _pageStatus.value = PageStatus.Fail
                        _hasMorePosts.value = false
                    }
                    return@launch
                }
            }

            _creatorData.value = dataResult.getOrNull()
            val page = postsResult.getOrNull()
            val posts = page?.list ?: emptyList()
            postTotalCount = page?.total ?: 0
            postPageNum = 1
            _postList.value = posts
            _hasMorePosts.value = posts.isNotEmpty() && posts.size < postTotalCount
            if (days != null) _selectedDays.value = days
            if (sortBy != null) _selectedSortBy.value = effectiveSortBy
            _pageStatus.value = PageStatus.Success
            Log.d(TAG, "loadAll: success, posts=${posts.size}, total=$postTotalCount, hasMore=${_hasMorePosts.value}")
        }
    }

    /**
     * 上拉加载更多：请求当前筛选条件下下一页并追加。
     */
    fun loadMorePosts() {
        if (!_hasMorePosts.value || _loadingMorePosts.value) return
        if (_pageStatus.value != PageStatus.Success) return
        viewModelScope.launch {
            _loadingMorePosts.value = true
            try {
                loadPostNextPage()
            } catch (e: Exception) {
                Log.e(TAG, "loadMorePosts failed", e)
                _hasMorePosts.value = false
            } finally {
                _loadingMorePosts.value = false
            }
        }
    }

    private suspend fun loadPostNextPage() {
        val nextPage = postPageNum + 1
        val days = _selectedDays.value
        val sortBy = _selectedSortBy.value.let { if (it == 5) 0 else it }
        Log.d(TAG, "loadPostNextPage: nextPage=$nextPage, days=$days, sortBy=$sortBy")
        repository.getPostDataList(
            days = days,
            pageNum = nextPage,
            pageSize = postPageSize,
            sortBy = sortBy
        )
            .onSuccess { page ->
                postTotalCount = page.total
                val newPosts = page.list
                if (newPosts.isEmpty()) {
                    _hasMorePosts.value = false
                    Log.d(TAG, "loadPostNextPage: empty page")
                    return@onSuccess
                }
                val merged = _postList.value + newPosts
                _postList.value = merged
                postPageNum = nextPage
                _hasMorePosts.value = merged.size < postTotalCount
                Log.d(
                    TAG,
                    "loadPostNextPage: merged=${merged.size}, total=$postTotalCount, hasMore=${_hasMorePosts.value}"
                )
            }
            .onFailure {
                Log.e(TAG, "loadPostNextPage failed", it)
                _hasMorePosts.value = false
            }
    }

    /**
     * 切换时间范围并重新加载。
     */
    fun setSelectedDays(days: Int) {
        _selectedDays.value = days
        loadAll(days = days)
    }

    /**
     * 切换排序方式并重新加载帖子列表。
     */
    fun setSortBy(sortBy: Int) {
        _selectedSortBy.value = sortBy
        loadAll(sortBy = sortBy)
    }

    /**
     * 跳转帖子详情。
     */
    fun openPostDetail(context: Context, item: CreatorStatisticsPostItem) {
        val intent = Intent(context, com.vortexa.ui.page.post.detail.PostDetailActivity::class.java).apply {
            putExtra("extra_post_id", item.postId.toString())
        }
        context.startActivity(intent)
    }

    companion object {
        private const val TAG = "DataCenterViewModel"
    }
}
