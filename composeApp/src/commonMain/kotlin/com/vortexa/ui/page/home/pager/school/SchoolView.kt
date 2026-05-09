package com.vortexa.ui.page.home.pager.school

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.ui.component.CollapsibleHeaderPagerScaffold
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.theme.belowStatusBar
import kotlinx.coroutines.launch

/**
 * 校园页：顶部搜索栏与 Banner 可整体上滑收起，TabBar 吸顶，底部为带独立滚动状态的 HorizontalPager。
 *
 * @author LuXin
 * @createTime 2026/1/19
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SchoolView() {
    var query by remember { mutableStateOf("") }
    val viewModel: SchoolViewModel = vortexaViewModel { SchoolViewModel() }
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pageStatus by viewModel.pageStatus.collectAsState()
    val selectedIndex = viewModel.selectedTabIndex
    val pagerState = rememberPagerState(
        initialPage = selectedIndex,
        pageCount = { SchoolTabs.size }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.settledPage) {
        viewModel.syncTabFromPage(pagerState.settledPage)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .belowStatusBar()
            .background(Color.White)
    ) {
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() }
        ) {
            CollapsibleHeaderPagerScaffold(
                pagerState = pagerState,
                pageCount = SchoolTabs.size,
                modifier = Modifier.fillMaxSize(),
                headerContent = {
                    SchoolHeaderContent(
                        query = query,
                        onQueryChange = { query = it },
                        onSubmit = { keyword ->
                            if (keyword.isNotBlank()) {
                                // 预留搜索行为，后续接接口时可直接复用。
                            }
                        }
                    )
                },
                stickyContent = {
                    SchoolTabBar(
                        selectedIndex = selectedIndex,
                        onTabClick = { index ->
                            scope.launch { pagerState.animateScrollToPage(index) }
                            viewModel.onTabClick(index)
                        }
                    )
                }
            ) { page, listState ->
                when (page) {
                    0 -> TeacherGuidance(listState = listState)
                    else -> VortexaSchoolList(listState = listState)
                }
            }
        }
        PageStatusView(
            status = pageStatus,
            modifier = Modifier.fillMaxSize(),
            onRefresh = { viewModel.refresh(showRefreshing = false) }
        )
    }
}

@Composable
fun SchoolPreview() {
    com.vortexa.ui.theme.BaseTheme { SchoolView() }
}
