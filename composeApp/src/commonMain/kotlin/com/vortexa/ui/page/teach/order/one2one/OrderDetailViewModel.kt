package com.vortexa.ui.page.teach.order.one2one

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.config.UserConfig
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.page.teach.helper.TeachingC2cRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 订单详情页 ViewModel。根据 [reserveId] 请求预约详情并映射为 [OrderDetailUi]。
 *
 * @param reserveId 预约 ID，由路由传入
 */
class OrderDetailViewModel(
    private val reserveId: Int
) : ViewModel() {

    private companion object {
        private const val TAG = "OrderDetailViewModel"
    }

    private val c2cRepository = TeachingC2cRepository()

    private val _pageStatus = MutableStateFlow<PageStatus>(PageStatus.Loading)
    /** 页面状态，供 PageStatusView 使用 */
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    private val _detailUi = MutableStateFlow<OrderDetailUi?>(null)
    /** 详情数据，Success 时非 null */
    val detailUi: StateFlow<OrderDetailUi?> = _detailUi.asStateFlow()

    private val _showCancelConfirm = MutableStateFlow(false)
    /** 是否显示取消预约确认弹窗 */
    val showCancelConfirm: StateFlow<Boolean> = _showCancelConfirm.asStateFlow()

    private val _cancelLoading = MutableStateFlow(false)
    /** 取消预约请求是否进行中，供确认弹窗「确定」按钮 Loading 使用 */
    val cancelLoading: StateFlow<Boolean> = _cancelLoading.asStateFlow()

    private val _cancelSuccess = MutableStateFlow(false)
    /** 取消预约成功，Activity 监听到后 finish 并调用 [clearCancelSuccess] */
    val cancelSuccess: StateFlow<Boolean> = _cancelSuccess.asStateFlow()

    init {
        loadDetail()
    }

    /** 打开取消预约确认弹窗 */
    fun openCancelConfirm() {
        _showCancelConfirm.value = true
    }

    /** 关闭取消预约确认弹窗 */
    fun dismissCancelConfirm() {
        _showCancelConfirm.value = false
    }

    /** 取消预约成功后由 Activity 调用，清空 [cancelSuccess] */
    fun clearCancelSuccess() {
        _cancelSuccess.value = false
    }

    /**
     * 提交取消预约（调用 POST /v/api/c2c/teacher/reserve/cancel）。
     * @param reason 取消原因，必填
     */
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
                }
        }
    }

    /** 请求预约详情并更新状态与 [detailUi]。 */
    fun loadDetail() {
        viewModelScope.launch {
            _pageStatus.value = PageStatus.Loading
            c2cRepository.getReserveDetail(reserveId)
                .onSuccess { detail ->
                    _detailUi.value = OrderDetailUi.from(detail, UserConfig.getUserId())
                    _pageStatus.value = PageStatus.Success
                    Log.d(TAG, "loadDetail success, reserveId=$reserveId")
                }
                .onFailure { e ->
                    Log.w(TAG, "loadDetail fail, reserveId=$reserveId", e)
                    _pageStatus.value = PageStatus.Fail
                }
        }
    }
}
