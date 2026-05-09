package com.vortexa.ui.page.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vortexa.ui.theme.Colors
import kotlinx.coroutines.launch

/**
 * 钱包页主视图：头部 + Tab（全部/获得/支出）+ HorizontalPager；每页为积分记录列表（最多 10 条可滚动）+ 底部分页。
 *
 * @param onBackClick 点击头部返回时回调
 */
@Composable
fun WalletView(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { WalletTabs.size }
    )
    val scope = rememberCoroutineScope()

    // 分页状态：当前页、总页数（示例 2/5）
    var currentPage by remember { mutableIntStateOf(1) }
    val totalPages = 5
    // 当前页记录数据（示例）
    val records = remember {
        mutableStateOf(
            listOf(
                WalletRecord("2025-10-02", "+50", "充值"),
                WalletRecord("2025-10-01", "-20", "兑换"),
                WalletRecord("2025-09-30", "+100", "充值")
            )
        )
    }

    LaunchedEffect(pagerState.settledPage) {
        selectedTabIndex = pagerState.settledPage
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        WalletHeader(onBackClick = onBackClick)
        WalletMediumInfo()
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .height(50.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomStart
        ) {
            WalletTabBar(
                selectedIndex = selectedTabIndex,
                onTabClick = { index ->
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                    selectedTabIndex = index
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    WalletRecordList(
                        records = records.value,
                        modifier = Modifier.weight(1f)
                    )
                    WalletPagination(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onPrevClick = {
                            if (currentPage > 1) currentPage -= 1
                        },
                        onNextClick = {
                            if (currentPage < totalPages) currentPage += 1
                        }
                    )
                }
            }
        )
    }
}

@Composable
private fun WalletPreview() {
    WalletView(onBackClick = {})
}
