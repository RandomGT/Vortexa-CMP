package com.vortexa.ui.page.teach.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vortexa.config.UserConfig
import com.vortexa.repository.C2cRepository
import com.vortexa.ui.component.pageStatus.PageStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 日程页 ViewModel。根据选中的日期请求该导师当天的预约时间槽。
 *
 * @param teacherId 导师 ID，由启动页（如导师个人主页）传入，无效时不再请求
 */
class ScheduleViewModel(
    private val teacherId: Long,
    private val c2cRepository: C2cRepository = C2cRepository()
) : ViewModel() {

    private val _pageStatus = MutableStateFlow<PageStatus>(PageStatus.Loading)
    /** 时间槽列表的请求状态，供 PageStatusView 使用 */
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    private val _timeSlots = MutableStateFlow<List<TimeSlotUi>>(emptyList())
    /** 当前选中日期下的时间槽列表 */
    val timeSlots: StateFlow<List<TimeSlotUi>> = _timeSlots.asStateFlow()

    private val _reserveLoading = MutableStateFlow(false)
    /** 预约请求是否进行中，供 LoadingButton 使用 */
    val reserveLoading: StateFlow<Boolean> = _reserveLoading.asStateFlow()

    /**
     * 加载指定日期的导师预约时间。由 View 在选中日期变化时调用。
     * @param date 选中的日期，null 时清空列表并置为 Empty
     */
    fun loadReserveTime(date: LocalDate?) {
        if (teacherId <= 0) {
            Log.w(TAG, "loadReserveTime: invalid teacherId=$teacherId")
            _pageStatus.value = PageStatus.Fail
            _timeSlots.value = emptyList()
            return
        }
        if (date == null) {
            _timeSlots.value = emptyList()
            _pageStatus.value = PageStatus.Empty
            return
        }
        viewModelScope.launch {
            _pageStatus.value = PageStatus.Loading
            val reserveDate = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
            Log.d(TAG, "loadReserveTime: teacherId=$teacherId reserveDate=$reserveDate")
            c2cRepository.getTeacherReserveTime(teacherId, reserveDate)
                .onSuccess { list ->
                    _timeSlots.value = list.map { item ->
                        TimeSlotUi(
                            label = item.reserveHour.replace("-", "~"),
                            isEnabled = item.canReserve,
                            reserveHour = item.reserveHour
                        )
                    }
                    _pageStatus.value = if (list.isEmpty()) PageStatus.Empty else PageStatus.Success
                    Log.d(TAG, "loadReserveTime: success, slots=${list.size}")
                }
                .onFailure {
                    _pageStatus.value = PageStatus.Fail
                    _timeSlots.value = emptyList()
                    Log.e(TAG, "loadReserveTime: failed", it)
                }
        }
    }

    /**
     * 提交预约。调用前需已选择日期与时间槽；Loading 由 [reserveLoading] 暴露，请求结束自动终止。
     * @param reserveDate 预约日期，格式 yyyy/MM/dd
     * @param reserveHour 时段，如 18:00-19:00
     */
    fun reserve(reserveDate: String, reserveHour: String) {
        val userId = UserConfig.getUserId()
        viewModelScope.launch {
            _reserveLoading.value = true
            Log.d(TAG, "reserve: teacherId=$teacherId reserveDate=$reserveDate reserveHour=$reserveHour userId=$userId")
            c2cRepository.reserve(teacherId, reserveDate, reserveHour, userId)
                .onSuccess { receipt ->
                    _reserveLoading.value = false
                    Log.i(TAG, "reserve: success reserveId=${receipt.reserveId} status=${receipt.status}")
                }
                .onFailure {
                    _reserveLoading.value = false
                    Log.e(TAG, "reserve: failed", it)
                }
        }
    }

    companion object {
        private const val TAG = "ScheduleVM"
    }
}

/**
 * 用于从 Activity 传入 teacherId 创建 [ScheduleViewModel]。
 */
class ScheduleViewModelFactory(private val teacherId: Long) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass != ScheduleViewModel::class.java) {
            throw IllegalArgumentException("ScheduleViewModelFactory only creates ScheduleViewModel")
        }
        return ScheduleViewModel(teacherId) as T
    }
}
