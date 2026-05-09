package com.vortexa.ui.page.teach.helper

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vortexa.lib_net.exception.ApiException
import com.vortexa.repository.C2cRepository
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.util.ToastUtil
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 课堂小助手：根据 [reserveId] 请求 GET /v/api/c2c/teacher/reserve/classroom 填充 UI。
 *
 * @param roleOverride 若由 Scheme 等传入（teacher/student），则覆盖仅用接口推断的角色。
 */
class ClassAssistantViewModel(
    private val reserveId: Int,
    private val roleOverride: ClassAssistantRole? = null
) : ViewModel() {

    private companion object {
        const val TAG = "ClassAssistantViewModel"
    }

    private val c2cRepository = C2cRepository()

    private val _pageStatus = MutableStateFlow(PageStatus.Loading)
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    private val _ui = MutableStateFlow<ClassAssistantUiState?>(null)
    val ui: StateFlow<ClassAssistantUiState?> = _ui.asStateFlow()

    private val _showCancelConfirm = MutableStateFlow(false)
    val showCancelConfirm: StateFlow<Boolean> = _showCancelConfirm.asStateFlow()

    private val _showTutorRejectConfirm = MutableStateFlow(false)
    /** 导师已接受、未到开课时间：取消预约确认（确认后走拒绝接口） */
    val showTutorRejectConfirm: StateFlow<Boolean> = _showTutorRejectConfirm.asStateFlow()

    private val _cancelLoading = MutableStateFlow(false)
    val cancelLoading: StateFlow<Boolean> = _cancelLoading.asStateFlow()

    private val _cancelSuccess = MutableStateFlow(false)
    val cancelSuccess: StateFlow<Boolean> = _cancelSuccess.asStateFlow()

    private val _tutorActionInProgress = MutableStateFlow(false)
    val tutorActionInProgress: StateFlow<Boolean> = _tutorActionInProgress.asStateFlow()

    private val _navigateToOrderDetailAfterAccept = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    /** 导师接受成功：跳转预约回执（订单详情）页 */
    val navigateToOrderDetailAfterAccept: SharedFlow<Int> = _navigateToOrderDetailAfterAccept.asSharedFlow()

    init {
        loadDetail(showFullScreenLoading = true)
    }

    fun openCancelConfirm() {
        _showCancelConfirm.value = true
    }

    fun dismissCancelConfirm() {
        _showCancelConfirm.value = false
    }

    fun openTutorRejectConfirm() {
        _showTutorRejectConfirm.value = true
    }

    fun dismissTutorRejectConfirm() {
        _showTutorRejectConfirm.value = false
    }

    /**
     * 导师端开课前「取消预约」：确认后调用拒绝接口（与待接受时的拒绝同源）。
     */
    fun confirmTutorRejectBeforeCourse() {
        viewModelScope.launch {
            _tutorActionInProgress.value = true
            c2cRepository.rejectReserve(reserveId, reason = null)
                .onSuccess {
                    Log.d(TAG, "confirmTutorRejectBeforeCourse success, reserveId=$reserveId")
                    _showTutorRejectConfirm.value = false
                    ToastUtil.show("取消成功")
                    loadDetail(showFullScreenLoading = false)
                }
                .onFailure { e ->
                    Log.w(TAG, "confirmTutorRejectBeforeCourse fail, reserveId=$reserveId", e)
                    toastApiFailure(e, "取消失败")
                }
            _tutorActionInProgress.value = false
        }
    }

    fun clearCancelSuccess() {
        _cancelSuccess.value = false
    }

    fun cancelReserve(reason: String) {
        viewModelScope.launch {
            _cancelLoading.value = true
            c2cRepository.cancelReserve(reserveId, reason)
                .onSuccess {
                    _cancelLoading.value = false
                    _showCancelConfirm.value = false
                    _cancelSuccess.value = true
                    Log.d(TAG, "cancelReserve success, reserveId=$reserveId")
                }
                .onFailure { e ->
                    _cancelLoading.value = false
                    Log.w(TAG, "cancelReserve fail, reserveId=$reserveId", e)
                    toastApiFailure(e, "取消失败")
                }
        }
    }

    /**
     * @param showFullScreenLoading true 时全屏 Loading（首屏、失败重试）；false 时仅静默刷新详情（接受/拒绝成功后）
     */
    fun loadDetail(showFullScreenLoading: Boolean = true) {
        viewModelScope.launch {
            if (showFullScreenLoading) {
                _pageStatus.value = PageStatus.Loading
            }
            c2cRepository.getReserveClassroom(reserveId)
                .onSuccess { detail ->
                    if (detail.isClassAssistantEmptyPayload()) {
                        _ui.value = null
                        _pageStatus.value = PageStatus.Empty
                        Log.d(TAG, "loadDetail empty payload, reserveId=$reserveId")
                    } else {
                        _ui.value = mapReserveClassroomToClassAssistantUi(reserveId, detail, roleOverride)
                        _pageStatus.value = PageStatus.Success
                        Log.d(TAG, "loadDetail success, reserveId=$reserveId")
                    }
                }
                .onFailure { e ->
                    Log.w(TAG, "loadDetail fail, reserveId=$reserveId", e)
                    if (showFullScreenLoading) {
                        _pageStatus.value = PageStatus.Fail
                    } else {
                        ToastUtil.show("刷新详情失败，请稍后重试")
                    }
                }
        }
    }

    /** 导师接受预约 */
    fun acceptReserve() {
        viewModelScope.launch {
            _tutorActionInProgress.value = true
            c2cRepository.acceptReserve(reserveId)
                .onSuccess {
                    Log.d(TAG, "acceptReserve success, reserveId=$reserveId")
                    ToastUtil.show("已接受预约")
                    _navigateToOrderDetailAfterAccept.emit(reserveId)
                }
                .onFailure { e ->
                    Log.w(TAG, "acceptReserve fail, reserveId=$reserveId", e)
                    toastApiFailure(e, "接受失败")
                }
            _tutorActionInProgress.value = false
        }
    }

    /**
     * 导师拒绝预约。
     * @param reason 拒绝原因，可选；无输入时传 null
     */
    fun rejectReserve(reason: String? = null) {
        viewModelScope.launch {
            _tutorActionInProgress.value = true
            c2cRepository.rejectReserve(reserveId, reason)
                .onSuccess {
                    Log.d(TAG, "rejectReserve success, reserveId=$reserveId")
                    ToastUtil.show("已拒绝该预约")
                    loadDetail(showFullScreenLoading = false)
                }
                .onFailure { e ->
                    Log.w(TAG, "rejectReserve fail, reserveId=$reserveId", e)
                    toastApiFailure(e, "拒绝失败")
                }
            _tutorActionInProgress.value = false
        }
    }

    private fun toastApiFailure(e: Throwable, fallback: String) {
        val msg = (e as? ApiException)?.message?.takeIf { it.isNotBlank() }
            ?: e.message?.takeIf { it.isNotBlank() }
            ?: fallback
        ToastUtil.show(msg)
    }
}

class ClassAssistantViewModelFactory(
    private val reserveId: Int,
    private val roleOverride: ClassAssistantRole? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass != ClassAssistantViewModel::class.java) {
            throw IllegalArgumentException("ClassAssistantViewModelFactory only creates ClassAssistantViewModel")
        }
        return ClassAssistantViewModel(reserveId, roleOverride) as T
    }
}
