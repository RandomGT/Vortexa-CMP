package com.vortexa.ui.page.creator

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.model.CreatorActivity
import com.vortexa.model.CreatorData
import com.vortexa.model.CreatorTask
import com.vortexa.model.CreatorUserInfo
import com.vortexa.ui.component.pageStatus.PageStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreatorViewModel(
    private val repository: CreatorRepository = CreatorRepository(),
) : ViewModel() {
    private val _pageStatus = MutableStateFlow<PageStatus>(PageStatus.Loading)
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    private val _creatorData = MutableStateFlow<CreatorData?>(null)
    val creatorData: StateFlow<CreatorData?> = _creatorData.asStateFlow()

    private val _creatorUserInfo = MutableStateFlow<CreatorUserInfo?>(null)
    val creatorUserInfo: StateFlow<CreatorUserInfo?> = _creatorUserInfo.asStateFlow()

    private val _activities = MutableStateFlow<List<CreatorActivity>>(emptyList())
    val activities: StateFlow<List<CreatorActivity>> = _activities.asStateFlow()

    private val _tasks = MutableStateFlow<List<CreatorTask>>(emptyList())
    val tasks: StateFlow<List<CreatorTask>> = _tasks.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll(days: Int = 7, silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _pageStatus.value = PageStatus.Loading
            try {
                coroutineScope {
                    val dataDeferred = async { repository.getCreatorData(days) }
                    val userInfoDeferred = async { repository.getCreatorUserInfo() }
                    val activitiesDeferred = async {
                        repository.getCreatorActivities(pageNum = 1, pageSize = 4)
                    }
                    val tasksDeferred = async {
                        repository.getCreatorTasks(pageNum = 1, pageSize = 10)
                    }

                    val dataResult = dataDeferred.await()
                    val userInfoResult = userInfoDeferred.await()
                    val activitiesResult = activitiesDeferred.await()
                    val tasksResult = tasksDeferred.await()

                    val failure = listOf(dataResult, userInfoResult, activitiesResult, tasksResult)
                        .firstOrNull { it.isFailure }
                    if (failure != null) {
                        Log.e(TAG, "loadAll failed", failure.exceptionOrNull())
                        if (!silent) _pageStatus.value = PageStatus.Fail
                        return@coroutineScope
                    }

                    _creatorData.value = dataResult.getOrNull()
                    _creatorUserInfo.value = userInfoResult.getOrNull()
                    _activities.value = activitiesResult.getOrNull()?.list.orEmpty()
                    _tasks.value = tasksResult.getOrNull()?.list.orEmpty()
                    _pageStatus.value = PageStatus.Success
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadAll unexpected error", e)
                if (!silent) _pageStatus.value = PageStatus.Fail
            }
        }
    }

    companion object {
        private const val TAG = "CreatorViewModel"
    }
}
