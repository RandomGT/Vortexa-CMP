package com.vortexa.ui.page.home.pager.school

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.model.RecommendCard
import com.vortexa.model.RecommendCourseItem
import com.vortexa.model.SchoolCourseCard
import com.vortexa.model.TeacherListItem
import com.vortexa.repository.C2cRepository
import com.vortexa.repository.HomeRepository
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.page.home.pager.profile.ProfileSyncCenter
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 校园页 ViewModel：管理 Tab 索引与「导师在线指导」列表的推荐卡片数据；
 * 导师列表支持筛选（标签、报价），通过 [C2cRepository] 调用 /v/api/c2c/teacher/list。
 */
class SchoolViewModel : ViewModel() {

    private companion object {
        const val TAG = "SchoolViewModel"
    }

    private val c2cRepository = C2cRepository()
    private val homeRepository = HomeRepository()

    /** 当前选中的 Tab 索引，0=导师在线指导，1=涡联学院 */
    var selectedTabIndex by mutableStateOf(0)
        private set

    private val _mentorCards = MutableStateFlow<List<RecommendCard>>(emptyList())
    /** 导师在线指导 Tab 的卡片列表，用于 RecommendGrid */
    val mentorCards: StateFlow<List<RecommendCard>> = _mentorCards.asStateFlow()

    /** 当前筛选条件：已选标签，用于筛选弹窗回显与请求参数 */
    private val _filterTags = MutableStateFlow<Set<String>>(emptySet())
    val filterTags: StateFlow<Set<String>> = _filterTags.asStateFlow()

    /** 当前最低报价筛选 */
    private val _filterMinPrice = MutableStateFlow("")
    val filterMinPrice: StateFlow<String> = _filterMinPrice.asStateFlow()

    /** 当前最高报价筛选 */
    private val _filterMaxPrice = MutableStateFlow("")
    val filterMaxPrice: StateFlow<String> = _filterMaxPrice.asStateFlow()

    private val _schoolListCards = MutableStateFlow<List<SchoolCourseCard>>(emptyList())
    /** 涡联学院/有声读物 Tab 的课程卡片列表，用于 Grid */
    val schoolListCards: StateFlow<List<SchoolCourseCard>> = _schoolListCards.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    /** 下拉刷新指示器是否与接口请求同步 */
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _pageStatus = MutableStateFlow(PageStatus.Loading)
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    init {
        observeProfileSync()
        refresh(showRefreshing = false)
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
     * 并发刷新导师列表与学院课程列表；与推荐页一致用 [coroutineScope] + [async]。
     *
     * @param showRefreshing 是否展示下拉刷新圈；首次进入传 false 避免指示器闪烁。
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
            var mentorOk = false
            var schoolOk = false
            try {
                coroutineScope {
                    val mentorTask = async { loadMentorCardsSuspend() }
                    val schoolTask = async { loadSchoolListCardsSuspend() }
                    mentorOk = mentorTask.await()
                    schoolOk = schoolTask.await()
                }
            } catch (e: Exception) {
                Log.e(TAG, "refresh failed", e)
                mentorOk = false
                schoolOk = false
            } finally {
                if (showRefreshing) {
                    _isRefreshing.value = false
                }
                _pageStatus.value = if (mentorOk && schoolOk) PageStatus.Success else PageStatus.Fail
                Log.i(TAG, "refresh end")
            }
        }
    }

    /**
     * suspend 版导师列表加载，供 [refresh] 复用。
     */
    private suspend fun loadMentorCardsSuspend(): Boolean {
        val tagsParam = _filterTags.value.takeIf { it.isNotEmpty() }?.joinToString(",")
        val min = _filterMinPrice.value.takeIf { it.isNotBlank() }
        val max = _filterMaxPrice.value.takeIf { it.isNotBlank() }
        val result = c2cRepository.getTeacherList(
            tags = tagsParam,
            minPrice = min,
            maxPrice = max,
            pageNum = 1,
            pageSize = 20
        )
        return result.fold(
            onSuccess = { data ->
                val cards = data.list.map(::teacherListItemToRecommendCard)
                _mentorCards.value = cards
                Log.d(TAG, "loadMentorCardsSuspend success, size=${cards.size}")
                true
            },
            onFailure = { e ->
                Log.w(TAG, "loadMentorCardsSuspend failed", e)
                _mentorCards.value = emptyList()
                false
            }
        )
    }

    private fun teacherListItemToRecommendCard(item: TeacherListItem): RecommendCard = RecommendCard(
        id = item.teacherId,
        title = item.nickName,
        tags = item.tagList ?: emptyList(),
        price = item.price.toFloatOrNull() ?: 0f,
        unit = item.priceUnit?.takeIf { it.isNotBlank() } ?: "积分",
        favorite = item.score.toFloatOrNull() ?: 0f,
        imageUrl = item.teacherAvatar?.takeIf { it.isNotBlank() } ?: item.avatar
    )

    /**
     * suspend 版学院课程列表加载，供 [refresh] 复用。
     */
    private suspend fun loadSchoolListCardsSuspend(): Boolean {
        val result = homeRepository.getRecommendCourse(
            pageNum = 1,
            pageSize = 4,
            userId = null
        )
        return result.fold(
            onSuccess = { data ->
                val cards = data.list.map(::recommendCourseItemToSchoolCard)
                _schoolListCards.value = cards
                Log.d(TAG, "loadSchoolListCardsSuspend success, size=${cards.size}, total=${data.total}")
                true
            },
            onFailure = { e ->
                Log.w(TAG, "loadSchoolListCardsSuspend failed", e)
                _schoolListCards.value = emptyList()
                false
            }
        )
    }

    /**
     * 将接口课程项映射为 UI 课程卡片；接口无 price/rating/tags 时使用默认值。
     */
    private fun recommendCourseItemToSchoolCard(item: RecommendCourseItem): SchoolCourseCard =
        SchoolCourseCard(
            id = item.courseId.toString(),
            title = item.title,
            teacherName = item.authorNickname ?: "",
            purchaseCount = "${item.studentCount}次",
            tags = emptyList(),
            price = 0f,
            unit = "USD",
            rating = 0f,
            coverUrl = item.cover,
            teacherAvatarUrl = item.avatar
        )

    /**
     * 应用筛选条件并重新加载导师列表；由筛选弹窗「确认」调用。
     */
    fun applyFilter(tags: Set<String>, minPrice: String, maxPrice: String) {
        _filterTags.value = tags
        _filterMinPrice.value = minPrice
        _filterMaxPrice.value = maxPrice
        refresh(showRefreshing = false)
    }

    /** 用户点击 Tab 时调用 */
    fun onTabClick(index: Int) {
        selectedTabIndex = index
    }

    /** 滑动 Pager 结束时同步 Tab 选中态 */
    fun syncTabFromPage(index: Int) {
        selectedTabIndex = index
    }
}
