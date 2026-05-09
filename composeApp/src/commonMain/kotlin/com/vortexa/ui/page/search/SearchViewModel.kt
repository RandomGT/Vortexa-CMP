package com.vortexa.ui.page.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.config.UserConfig
import com.vortexa.model.TeacherItem
import com.vortexa.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 搜索页 ViewModel：热搜话题来自接口 /v/api/home/search/suggest，导师推荐来自 /v/api/home/recommend/teachers。
 */
class SearchViewModel(
    private val homeRepository: HomeRepository = HomeRepository()
) : ViewModel() {

    /** 热搜话题，来自 getSearchSuggest 接口 */
    private val _hotTopics = MutableStateFlow<List<String>>(emptyList())
    val hotTopics: StateFlow<List<String>> = _hotTopics.asStateFlow()

    /** 导师推荐列表，与首页热门导师同源接口 */
    private val _tutorList = MutableStateFlow<List<TeacherItem>>(emptyList())
    val tutorList: StateFlow<List<TeacherItem>> = _tutorList.asStateFlow()

    init {
        loadHotTopics()
        loadRecommendTutors()
    }

    /** 加载热搜话题，调用 /v/api/home/search/suggest（不传 keyword） */
    private fun loadHotTopics() {
        viewModelScope.launch {
            homeRepository.getSearchSuggest()
                .onSuccess { list ->
                    _hotTopics.value = list
                    Log.d(TAG, "Loaded ${list.size} search suggestions")
                }
                .onFailure {
                    Log.e(TAG, "Failed to load search suggest", it)
                }
        }
    }

    private fun loadRecommendTutors() {
        viewModelScope.launch {
            val userId = UserConfig.getUserId()
            homeRepository.getRecommendTeachers(userId = if (userId > 0) userId else null)
                .onSuccess { response ->
                    _tutorList.value = response.list
                    Log.d(TAG, "Loaded ${response.list.size} recommend tutors")
                }
                .onFailure {
                    Log.e(TAG, "Failed to load recommend tutors", it)
                    _tutorList.value = emptyList()
                }
        }
    }

    private companion object {
        const val TAG = "SearchViewModel"
    }
}
