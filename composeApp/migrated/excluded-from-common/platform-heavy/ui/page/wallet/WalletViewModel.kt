package com.vortexa.ui.page.wallet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.net.auth.isLoginRequired
import com.vortexa.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 钱包页 ViewModel。
 * 负责积分余额获取（GET /v/api/user/wallet/point）及余额状态管理。
 *
 * @param repository 用户数据仓库，默认使用 [UserRepository]
 * @author LuXin
 */
class WalletViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _balance = MutableStateFlow(0)
    /** 当前积分余额 */
    val balance: StateFlow<Int> = _balance.asStateFlow()

    init {
        loadBalance()
    }

    /**
     * 加载积分余额。
     * 从接口获取可用积分并更新 _balance。
     */
    fun loadBalance() {
        viewModelScope.launch {
            repository.getWalletPoint()
                .onSuccess { data ->
                    _balance.value = data.availablePoints
                    Log.d(TAG, "loadBalance: success, availablePoints=${data.availablePoints}")
                }
                .onFailure {
                    _balance.value = 0
                    if (it.isLoginRequired()) {
                        Log.i(TAG, "loadBalance: guest, skip")
                    } else {
                        Log.e(TAG, "loadBalance: failed", it)
                    }
                }
        }
    }

    companion object {
        private const val TAG = "WalletViewModel"
    }
}