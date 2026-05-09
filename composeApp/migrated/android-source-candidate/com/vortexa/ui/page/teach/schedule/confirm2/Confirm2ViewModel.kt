package com.vortexa.ui.page.teach.schedule.confirm2

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vortexa.config.UserConfig
import com.vortexa.repository.C2cRepository
import com.vortexa.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * 支付确认页 ViewModel。持有预约参数，预加载导师信息与积分余额；调用预约接口并在请求期间暴露 [payLoading]。
 *
 * @param teacherId 导师 ID
 * @param reserveDate 预约日期，格式 yyyy/MM/dd
 * @param reserveHour 时段，如 18:00-19:00
 */
class Confirm2ViewModel(
    private val teacherId: Long,
    private val reserveDate: String,
    private val reserveHour: String,
    private val c2cRepository: C2cRepository = C2cRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _teacherDisplayName = MutableStateFlow("")
    val teacherDisplayName: StateFlow<String> = _teacherDisplayName.asStateFlow()

    private val _teacherAvatarUrl = MutableStateFlow<String?>(null)
    val teacherAvatarUrl: StateFlow<String?> = _teacherAvatarUrl.asStateFlow()

    private val _guideFeeText = MutableStateFlow("—")
    val guideFeeText: StateFlow<String> = _guideFeeText.asStateFlow()

    private val _balancePointsText = MutableStateFlow("—")
    val balancePointsText: StateFlow<String> = _balancePointsText.asStateFlow()

    private val _totalPointsText = MutableStateFlow("—")
    val totalPointsText: StateFlow<String> = _totalPointsText.asStateFlow()

    /** 本次指导应付积分（与「总计」一致，一期无优惠券） */
    private var guidePoints: Int = 0

    init {
        loadConfirmPreview()
    }

    private fun loadConfirmPreview() {
        if (teacherId <= 0) {
            Log.w(TAG, "loadConfirmPreview: invalid teacherId")
            return
        }
        viewModelScope.launch {
            val hours = reserveHourToDurationHours(reserveHour)
            coroutineScope {
                val teacherJob = async { c2cRepository.getTeacherDetail(teacherId) }
                val walletJob = async { userRepository.getWalletPoint() }
                val teacherResult = teacherJob.await()
                val walletResult = walletJob.await()
                teacherResult.onSuccess { response ->
                    val base = response.baseInfo
                    _teacherDisplayName.value = base.nickName
                    _teacherAvatarUrl.value = base.avatar?.takeIf { it.isNotBlank() }
                    val unit = base.price.coerceAtLeast(0f)
                    guidePoints = ceil(unit * hours).roundToInt().coerceAtLeast(0)
                    val feeLabel = formatPoints(guidePoints)
                    _guideFeeText.value = feeLabel
                    _totalPointsText.value = feeLabel
                }.onFailure {
                    Log.e(TAG, "loadConfirmPreview: teacher failed", it)
                    _guideFeeText.value = "—"
                    _totalPointsText.value = "—"
                }
                walletResult.onSuccess { data ->
                    _balancePointsText.value = formatPoints(data.availablePoints)
                }.onFailure {
                    Log.e(TAG, "loadConfirmPreview: wallet failed", it)
                    _balancePointsText.value = "—"
                }
            }
        }
    }

    private val _payLoading = MutableStateFlow(false)
    /** 预约/支付请求是否进行中，供底部「确认支付」LoadingButton 使用 */
    val payLoading: StateFlow<Boolean> = _payLoading.asStateFlow()

    private val _reserveSuccessReserveId = MutableStateFlow<Long?>(null)
    /** 预约成功后的 reserveId，由 Activity 监听并跳转订单详情页后调用 [clearReserveSuccess] 清空 */
    val reserveSuccessReserveId: StateFlow<Long?> = _reserveSuccessReserveId.asStateFlow()

    /**
     * 提交预约（调用 POST /v/api/c2c/teacher/reserve）。请求前将 [payLoading] 置为 true，结束（成功或失败）后置为 false；成功时写入 [reserveSuccessReserveId]。
     */
    fun reserve() {
        val userId = UserConfig.getUserId()
        viewModelScope.launch {
            _payLoading.value = true
            Log.d(TAG, "reserve: teacherId=$teacherId reserveDate=$reserveDate reserveHour=$reserveHour userId=$userId")
            c2cRepository.reserve(teacherId, reserveDate, reserveHour, userId)
                .onSuccess { receipt ->
                    _payLoading.value = false
                    _reserveSuccessReserveId.value = receipt.reserveId
                    Log.i(TAG, "reserve: success reserveId=${receipt.reserveId} status=${receipt.status}")
                }
                .onFailure {
                    _payLoading.value = false
                    Log.e(TAG, "reserve: failed", it)
                }
        }
    }

    /** 跳转订单详情后由 Activity 调用，清空 [reserveSuccessReserveId] 避免重复处理 */
    fun clearReserveSuccess() {
        _reserveSuccessReserveId.value = null
    }

    companion object {
        private const val TAG = "Confirm2VM"
    }
}

private fun formatPoints(points: Int): String = "${points}积分"

/** 与确认页文案一致：按时段起止小时之差计时长，至少 1 小时 */
private fun reserveHourToDurationHours(reserveHour: String): Float {
    if (reserveHour.isEmpty()) return 2f
    val parts = reserveHour.split("-")
    if (parts.size != 2) return 1f
    val start = parts[0].trim().substringBefore(":")
    val end = parts[1].trim().substringBefore(":")
    val startH = start.toIntOrNull() ?: 0
    val endH = end.toIntOrNull() ?: 0
    return (endH - startH).coerceAtLeast(1).toFloat()
}

/** 用于从 Activity 传入预约参数创建 [Confirm2ViewModel]。 */
class Confirm2ViewModelFactory(
    private val teacherId: Long,
    private val reserveDate: String,
    private val reserveHour: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass != Confirm2ViewModel::class.java) {
            throw IllegalArgumentException("Confirm2ViewModelFactory only creates Confirm2ViewModel")
        }
        return Confirm2ViewModel(teacherId, reserveDate, reserveHour) as T
    }
}
