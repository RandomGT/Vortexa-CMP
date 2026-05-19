package com.vortexa.ui.page.wallet

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DealDetailViewModel(
    initialState: DealDetailState = DealDetailState()
) : ViewModel() {
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<DealDetailState> = _uiState.asStateFlow()
}

