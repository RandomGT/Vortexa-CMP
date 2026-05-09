package com.vortexa.ui.page.teach.profile

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.model.SchoolCourseCard
import com.vortexa.model.TeacherDetailBaseInfo
import com.vortexa.repository.C2cRepository
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.page.teach.schedule.ScheduleActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 学员评价单条数据（Figma 283-30403）。
 *
 * @param id 唯一标识
 * @param userName 用户昵称
 * @param timeAgo 相对时间文案，如 "2 hour ago"
 * @param starCount 星级 0f..5f，支持小数（半星等）
 * @param title 评价标题/摘要
 * @param content 评价正文
 */
data class TeacherReviewItem(
    val id: String,
    val userName: String,
    val timeAgo: String,
    val starCount: Float,
    val title: String,
    val content: String
)

/**
 * 导师个人主页 ViewModel。
 * 通过 [loadDetail] 拉取 /v/api/c2c/teacher/detail，暴露 baseInfo 与 pageStatus；
 * 学员评价、课程展示待对接接口，当前为空列表。
 */
class TeacherProfileViewModel(
    private val c2cRepository: C2cRepository = C2cRepository()
) : ViewModel() {

    private val _pageStatus = MutableStateFlow<PageStatus>(PageStatus.Loading)
    /** 页面请求状态，供 [PageStatusView] 使用 */
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    private val _baseInfo = MutableStateFlow<TeacherDetailBaseInfo?>(null)
    /** 导师基础信息，接口成功后供 Header/Content 展示 */
    val baseInfo: StateFlow<TeacherDetailBaseInfo?> = _baseInfo.asStateFlow()

    private val _reviewList = MutableStateFlow<List<TeacherReviewItem>>(emptyList())
    /** 学员评价列表（暂缓接口），供「学员评价」Tab 消费 */
    val reviewList: StateFlow<List<TeacherReviewItem>> = _reviewList.asStateFlow()

    private val _courseList = MutableStateFlow<List<SchoolCourseCard>>(emptyList())
    /** 课程展示列表（暂缓接口），供「课程展示」Tab 使用 */
    val courseList: StateFlow<List<SchoolCourseCard>> = _courseList.asStateFlow()

    init {
        loadReviewList()
        loadCourseList()
    }

    /**
     * 加载导师个人主页详情。由 View 在拿到 teacherId 后调用。
     * @param teacherId 导师 ID，无效（&lt;=0）时不请求并置为 Fail
     */
    fun loadDetail(teacherId: Long) {
        if (teacherId <= 0) {
            Log.w(TAG, "loadDetail: invalid teacherId=$teacherId")
            _pageStatus.value = PageStatus.Fail
            return
        }
        viewModelScope.launch {
            _pageStatus.value = PageStatus.Loading
            Log.d(TAG, "loadDetail: teacherId=$teacherId")
            c2cRepository.getTeacherDetail(teacherId)
                .onSuccess { response ->
                    _baseInfo.value = response.baseInfo
                    _pageStatus.value = PageStatus.Success
                    Log.d(TAG, "loadDetail: success")
                }
                .onFailure {
                    _pageStatus.value = PageStatus.Fail
                    Log.e(TAG, "loadDetail: failed", it)
                }
        }
    }

    /** 刷新详情，使用当前已加载的 baseInfo 的 teacherId 重新请求 */
    fun refreshDetail() {
        _baseInfo.value?.teacherId?.let { loadDetail(it) }
    }

    /**
     * 加载学员评价列表。暂缓接口，当前为空。
     */
    private fun loadReviewList() {
        viewModelScope.launch {
            _reviewList.value = emptyList<TeacherReviewItem>()
            Log.d(TAG, "loadReviewList: count=${_reviewList.value.size}")
        }
    }

    /**
     * 加载课程展示列表。暂缓接口，当前为空。
     */
    private fun loadCourseList() {
        viewModelScope.launch {
            _courseList.value = emptyList<SchoolCourseCard>()
            Log.d(TAG, "loadCourseList: count=${_courseList.value.size}")
        }
    }

    /** 点击预约，跳转预约页并传入当前导师 ID */
    fun onScheduleClick(context: Context) {
        val teacherId = _baseInfo.value?.teacherId ?: -1L
        ScheduleActivity.start(context, teacherId)
    }
}

private const val TAG = "TeacherProfileVM"
