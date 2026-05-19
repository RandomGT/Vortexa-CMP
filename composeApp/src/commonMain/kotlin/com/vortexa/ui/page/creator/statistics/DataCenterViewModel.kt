package com.vortexa.ui.page.creator.statistics

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.model.CreatorData
import com.vortexa.model.CreatorStatisticsPostItem
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.page.creator.CreatorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DataCenterViewModel(
    private val repository: CreatorRepository = CreatorRepository(),
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
    val selectedSortBy: StateFlow<Int> = _selectedSortBy.asStateFlow()

    private val _hasMorePosts = MutableStateFlow(false)
    val hasMorePosts: StateFlow<Boolean> = _hasMorePosts.asStateFlow()

    private val _loadingMorePosts = MutableStateFlow(false)
    val loadingMorePosts: StateFlow<Boolean> = _loadingMorePosts.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll(days: Int? = null, sortBy: Int? = null, silent: Boolean = false) {
        val effectiveDays = days ?: _selectedDays.value
        val effectiveSortBy = sortBy ?: _selectedSortBy.value
        viewModelScope.launch {
            _loadingMorePosts.value = false
            if (!silent) _pageStatus.value = PageStatus.Loading

            val dataResult = repository.getCreatorData(effectiveDays)
            val postsResult = repository.getPostDataList(
                days = effectiveDays,
                pageNum = 1,
                pageSize = postPageSize,
                sortBy = effectiveSortBy,
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
            val posts = page?.list.orEmpty()
            postTotalCount = page?.total ?: 0
            postPageNum = 1
            _postList.value = posts
            _hasMorePosts.value = posts.isNotEmpty() && posts.size < postTotalCount
            if (days != null) _selectedDays.value = days
            if (sortBy != null) _selectedSortBy.value = effectiveSortBy
            _pageStatus.value = PageStatus.Success
        }
    }

    fun loadMorePosts() {
        if (!_hasMorePosts.value || _loadingMorePosts.value) return
        if (_pageStatus.value != PageStatus.Success) return
        viewModelScope.launch {
            _loadingMorePosts.value = true
            try {
                loadPostNextPage()
            } finally {
                _loadingMorePosts.value = false
            }
        }
    }

    private suspend fun loadPostNextPage() {
        val nextPage = postPageNum + 1
        repository.getPostDataList(
            days = _selectedDays.value,
            pageNum = nextPage,
            pageSize = postPageSize,
            sortBy = _selectedSortBy.value,
        )
            .onSuccess { page ->
                postTotalCount = page.total
                val newPosts = page.list
                if (newPosts.isEmpty()) {
                    _hasMorePosts.value = false
                    return@onSuccess
                }
                val merged = _postList.value + newPosts
                _postList.value = merged
                postPageNum = nextPage
                _hasMorePosts.value = merged.size < postTotalCount
            }
            .onFailure {
                Log.e(TAG, "loadPostNextPage failed", it)
                _hasMorePosts.value = false
            }
    }

    fun setSelectedDays(days: Int) {
        _selectedDays.value = days
        loadAll(days = days)
    }

    fun setSortBy(sortBy: Int) {
        _selectedSortBy.value = sortBy
        loadAll(sortBy = sortBy)
    }

    companion object {
        private const val TAG = "DataCenterViewModel"
    }
}
