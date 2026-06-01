package com.vortexa.ui.page.wallet.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.vortexa.ui.page.wallet.DealDetailState
import com.vortexa.ui.page.wallet.DealDetailViewModel
import com.vortexa.ui.viewmodel.vortexaViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.vortexa.ui.theme.belowStatusBar

@Composable
fun DealDetailView(
    onBackClick: () -> Unit = {},
    onRecordClick: () -> Unit = {},
    onQuestionClick: () -> Unit = {},
    onContactClick: () -> Unit = {},
    viewModel: DealDetailViewModel = vortexaViewModel { DealDetailViewModel() },
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    DealDetailContent(
        state = state,
        onBackClick = onBackClick,
        onRecordClick = onRecordClick,
        onQuestionClick = onQuestionClick,
        onContactClick = onContactClick,
        modifier = modifier
    )
}

@Composable
private fun DealDetailContent(
    state: DealDetailState,
    onBackClick: () -> Unit,
    onRecordClick: () -> Unit,
    onQuestionClick: () -> Unit,
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .belowStatusBar()
            .background(Color.White)
    ) {
        DealDetailToolbar(onBackClick = onBackClick)
        DealDetailBody(
            statusText = state.statusText,
            amountDisplay = state.amountDisplay,
            rows = state.detailRows,
            modifier = Modifier.weight(1f)
        )
        DealDetailBottomBar(
            onRecordClick = onRecordClick,
            onQuestionClick = onQuestionClick,
            onContactClick = onContactClick
        )
    }
}
