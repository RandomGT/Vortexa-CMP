package com.vortexa.ui.page.search.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.page.search.result.composite.CompositePage
import com.vortexa.ui.page.search.result.post.PostPage
import com.vortexa.ui.viewmodel.vortexaViewModel

/**
 * 为 `true` 时展示 TabBar + 多页 Pager；为 `false` 时仅展示帖文列表并隐藏 Tab、禁止滑动切换。
 * 多 Tab 能力保留，后续产品开放时改为 `true` 即可。
 */
private const val SHOW_SEARCH_RESULT_TAB_BAR = true

/**
 * 搜索结果页：顶部为 TabBar（综合/帖文/用户/导师/工具箱/课程），下方为 HorizontalPager 对应 6 个子页。
 * 点击 Tab 切换页；滑动 Pager 会同步更新选中 Tab。
 *
 * @param keyword 搜索关键词，来自搜索框提交
 */
@Composable
fun SearchResultView(keyword: String = "") {
    val viewModel = vortexaViewModel { SearchResultViewModel() }
    val selectedIndex = viewModel.selectedTabIndex

    val apiTabIndex = if (SHOW_SEARCH_RESULT_TAB_BAR) {
        selectedIndex
    } else {
        SEARCH_RESULT_POST_TAB_INDEX
    }
    LaunchedEffect(keyword, apiTabIndex) {
        if (keyword.isNotBlank()) {
            viewModel.loadSearchResult(keyword, apiTabIndex)
        }
    }

    val pageCount = if (SHOW_SEARCH_RESULT_TAB_BAR) SearchResultTabs.size else 1
    val postPageIndex = if (SHOW_SEARCH_RESULT_TAB_BAR) SEARCH_RESULT_POST_TAB_INDEX else 0
    val pagerState = rememberPagerState(
        initialPage = if (SHOW_SEARCH_RESULT_TAB_BAR) selectedIndex.coerceIn(0, pageCount - 1) else 0,
        pageCount = { pageCount }
    )
    val scope = rememberCoroutineScope()

    if (SHOW_SEARCH_RESULT_TAB_BAR) {
        LaunchedEffect(pagerState.settledPage) {
            viewModel.syncTabFromPage(pagerState.settledPage)
        }
    }

    Column(Modifier.fillMaxSize()) {
        if (SHOW_SEARCH_RESULT_TAB_BAR) {
            SearchResultTabBar(
                selectedIndex = selectedIndex,
                onTabClick = { index ->
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                    viewModel.onTabClick(index)
                }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = pageCount,
                userScrollEnabled = SHOW_SEARCH_RESULT_TAB_BAR,
                pageContent = { page ->
                    when (page) {
                        SEARCH_RESULT_GENERAL_TAB_INDEX -> CompositePage(viewModel = viewModel)
                        postPageIndex -> PostPage(viewModel = viewModel)
                        else -> UnsupportedSearchResultPage()
                    }
                }
            )
        }
    }
}

@Composable
fun SearchResultPreview() {
    SearchResultView()
}

@Composable
private fun UnsupportedSearchResultPage() {
    PageStatusView(
        status = PageStatus.Empty,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        emptyMessage = "暂无搜索结果"
    )
}
