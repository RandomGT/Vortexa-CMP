package com.vortexa.ui.page.creator

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.model.CreatorActivity
import com.vortexa.model.CreatorData
import com.vortexa.model.CreatorTask
import com.vortexa.model.CreatorUserInfo
import com.vortexa.repository.CreatorRepository
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.page.profile.interaction.InteractionActivity
import com.vortexa.ui.page.profile.paper.management.PaperManagementActivity
import com.vortexa.util.extension.routeToPage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 创作者中心 ViewModel。
 * 负责加载创作数据、用户信息、活动列表，管理页面状态。
 *
 * @param repository 创作中心数据仓库，默认 [CreatorRepository]
 * @author LuXin
 */
class CreatorViewModel(
    private val repository: CreatorRepository = CreatorRepository()
) : ViewModel() {

    private val _pageStatus = MutableStateFlow<PageStatus>(PageStatus.Loading)
    /** 页面请求状态，供 [PageStatusView] 使用 */
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    private val _creatorData = MutableStateFlow<CreatorData?>(null)
    /** 近 x 日创作数据 */
    val creatorData: StateFlow<CreatorData?> = _creatorData.asStateFlow()

    private val _creatorUserInfo = MutableStateFlow<CreatorUserInfo?>(null)
    /** 创作者用户信息 */
    val creatorUserInfo: StateFlow<CreatorUserInfo?> = _creatorUserInfo.asStateFlow()

    private val _activities = MutableStateFlow<List<CreatorActivity>>(emptyList())
    /** 活动 Banner 列表 */
    val activities: StateFlow<List<CreatorActivity>> = _activities.asStateFlow()

    private val _tasks = MutableStateFlow<List<CreatorTask>>(emptyList())
    /** 激励任务列表 */
    val tasks: StateFlow<List<CreatorTask>> = _tasks.asStateFlow()

    init {
        loadAll()
    }

    /**
     * 加载创作中心全部数据：数据卡片、用户信息、活动列表、激励任务。
     * 四个接口并行请求，非静默模式下任一失败则页面展示 Fail 状态。
     *
     * @param silent 为 true 时不进入全屏 Loading，失败时保留当前展示（用于从子页返回后的刷新）。
     */
    fun loadAll(days: Int = 7, silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _pageStatus.value = PageStatus.Loading
            }
            Log.d(TAG, "loadAll: days=$days, silent=$silent, start")
            coroutineScope {
                val dataDeferred = async { repository.getCreatorData(days) }
                val userInfoDeferred = async { repository.getCreatorUserInfo() }
                val activitiesDeferred = async {
                    repository.getCreatorActivities(pageNum = 1, pageSize = 4)
                }
                val tasksDeferred = async {
                    repository.getCreatorTasks(pageNum = 1, pageSize = 10)  // 多拉几条以支持滚动
                }
                val dataResult = dataDeferred.await()
                val userInfoResult = userInfoDeferred.await()
                val activitiesResult = activitiesDeferred.await()
                val tasksResult = tasksDeferred.await()
                when {
                    dataResult.isFailure -> {
                        Log.e(TAG, "loadAll: getCreatorData failed", dataResult.exceptionOrNull())
                        if (!silent) _pageStatus.value = PageStatus.Fail
                        return@coroutineScope
                    }
                    userInfoResult.isFailure -> {
                        Log.e(TAG, "loadAll: getCreatorUserInfo failed", userInfoResult.exceptionOrNull())
                        if (!silent) _pageStatus.value = PageStatus.Fail
                        return@coroutineScope
                    }
                    activitiesResult.isFailure -> {
                        Log.e(TAG, "loadAll: getCreatorActivities failed", activitiesResult.exceptionOrNull())
                        if (!silent) _pageStatus.value = PageStatus.Fail
                        return@coroutineScope
                    }
                    tasksResult.isFailure -> {
                        Log.e(TAG, "loadAll: getCreatorTasks failed", tasksResult.exceptionOrNull())
                        if (!silent) _pageStatus.value = PageStatus.Fail
                        return@coroutineScope
                    }
                }
                _creatorData.value = dataResult.getOrNull()
                _creatorUserInfo.value = userInfoResult.getOrNull()
                _activities.value = activitiesResult.getOrNull()?.list ?: emptyList()
                _tasks.value = tasksResult.getOrNull()?.list ?: emptyList()
                _pageStatus.value = PageStatus.Success
                Log.d(TAG, "loadAll: success")
            }
        }
    }

    /** 跳转互动管理页 */
    fun jumpToInteraction(context: Context) {
        val intent = Intent(context, InteractionActivity::class.java)
        context.startActivity(intent)
    }

    fun onPaperManagementClick(context: Context) {
        context.routeToPage(PaperManagementActivity::class.java)
    }

    companion object {
        private const val TAG = "CreatorViewModel"
    }
}
