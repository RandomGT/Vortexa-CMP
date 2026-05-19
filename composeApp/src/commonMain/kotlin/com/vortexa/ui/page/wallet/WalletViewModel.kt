package com.vortexa.ui.page.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 钱包页 ViewModel。
 * 负责积分余额获取（GET /v/api/user/wallet/point）及钱包记录分页状态。
 */
class WalletViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

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

    fun selectTab(index: Int) {
        if (index !in WalletTabs.indices) return
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    fun previousPage(tabIndex: Int) {
        _uiState.update { state ->
            val current = state.currentPage(tabIndex)
            state.copy(pageByTab = state.pageByTab + (tabIndex to (current - 1).coerceAtLeast(1)))
        }
    }

    fun nextPage(tabIndex: Int) {
        _uiState.update { state ->
            val current = state.currentPage(tabIndex)
            val total = state.totalPages(tabIndex)
            state.copy(pageByTab = state.pageByTab + (tabIndex to (current + 1).coerceAtMost(total)))
        }
    }
}

