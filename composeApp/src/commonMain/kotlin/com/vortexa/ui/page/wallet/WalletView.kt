package com.vortexa.ui.page.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vortexa.ui.viewmodel.vortexaViewModel
import kotlinx.coroutines.launch

@Composable
fun WalletView(
    onBackClick: () -> Unit = {},
    onRechargeClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {},
    onRecordClick: (WalletRecord) -> Unit = {},
    viewModel: WalletViewModel = vortexaViewModel { WalletViewModel() },
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = state.selectedTabIndex,
        pageCount = { WalletTabs.size }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.settledPage) {
        viewModel.selectTab(pagerState.settledPage)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        WalletHeader(
            balance = state.balance,
            onBackClick = onBackClick,
            onRechargeClick = onRechargeClick
        )
        WalletMediumInfo(onWithdrawClick = onWithdrawClick)
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .height(50.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomStart
        ) {
            WalletTabBar(
                selectedIndex = state.selectedTabIndex,
                onTabClick = { index ->
                    viewModel.selectTab(index)
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 4.dp, start = 18.dp)
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            userScrollEnabled = true,
            beyondViewportPageCount = WalletTabs.size,
            pageContent = { page ->
                val tabRecords = state.recordsForTab(page)
                val totalPages = state.totalPages(page)
                val currentPage = state.currentPage(page).coerceIn(1, totalPages)
                val startIndex = (currentPage - 1) * WALLET_PAGE_SIZE
                val pageRecords = tabRecords.drop(startIndex).take(WALLET_PAGE_SIZE)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    WalletRecordList(
                        records = pageRecords,
                        onRecordClick = onRecordClick,
                        modifier = Modifier.weight(1f)
                    )
                    WalletPagination(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onPrevClick = { viewModel.previousPage(page) },
                        onNextClick = { viewModel.nextPage(page) }
                    )
                }
            }
        )
    }
}
