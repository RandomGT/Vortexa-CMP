package com.vortexa.ui.page.wallet.pay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.repository.UserRepository
import com.vortexa.util.ToastUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PointRechargeUiState(
    val balance: Int = 0,
    val amountText: String = "",
    val selectedChannel: PointRechargePayChannel = PointRechargePayChannel.WeChat,
    val agreed: Boolean = false,
    val submitLoading: Boolean = false,
    val toastMessage: String? = null
)

class PointRechargeViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PointRechargeUiState())
    val uiState: StateFlow<PointRechargeUiState> = _uiState.asStateFlow()

    init {
        loadBalance()
    }

    fun loadBalance() {
        viewModelScope.launch {
            repository.getWalletPoint()
                .onSuccess { data ->
                    _uiState.update { it.copy(balance = data.availablePoints) }
                }
                .onFailure {
                    _uiState.update { it.copy(balance = 0) }
                }
        }
    }

    fun onAmountChange(value: String) {
        if (value.length <= 9 && (value.isEmpty() || value.all { it.isDigit() })) {
            _uiState.update { it.copy(amountText = value) }
        }
    }

    fun onChannelSelect(channel: PointRechargePayChannel) {
        _uiState.update { it.copy(selectedChannel = channel) }
    }

    fun onAgreedChange(agreed: Boolean) {
        _uiState.update { it.copy(agreed = agreed) }
    }

    fun submit() {
        val state = _uiState.value
        val points = state.amountText.toIntOrNull() ?: 0
        when {
            points <= 0 -> {
                emitToast("请输入积分数量")
                return
            }
            !state.agreed -> {
                emitToast("请先阅读并同意协议")
                return
            }
            else -> {
                emitToast("支付 SDK 暂未接入，当前仅保留充值页面")
            }
        }
    }

    fun consumeToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    private fun emitToast(message: String) {
        _uiState.update { it.copy(toastMessage = message, submitLoading = false) }
        ToastUtil.show(message)
    }
}
