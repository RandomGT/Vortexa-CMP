package com.vortexa.ui.page.home.pager.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import android.content.Context
import com.vortexa.ui.page.home.pager.home.communicate.CommunicateView
import com.vortexa.ui.theme.belowStatusBar
import com.vortexa.ui.page.home.pager.home.recommend.RecommendView
import com.vortexa.ui.viewmodel.vortexaViewModel

/**
 *  desc : Home Tab View
 *
 *  @author LuXin
 *  @createTime 2026/1/19
 */


@Composable
fun HomeView(
    onNavigateToSchool: (() -> Unit)? = null
) {
    Log.d("HomeView", "compose start")
    val viewModel = vortexaViewModel { HomeTabViewModel() }
    val context = Context()
    val lifecycleOwner = LocalLifecycleOwner.current

    fun applyPendingCommunicateTabSwitch() {
        if (HomeCommunicateNavigation.consumePendingSwitchToCommunicateTab()) {
            viewModel.onTabSelected(1)
        }
    }

    LaunchedEffect(Unit) {
        Log.d("HomeView", "LaunchedEffect(Unit)")
        applyPendingCommunicateTabSwitch()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                applyPendingCommunicateTabSwitch()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        Modifier.fillMaxSize()
            .belowStatusBar()
            .background(Color.White)
    ) {
            // Header
            HomeHeader(
                currentTab = viewModel.currentTab,
                onTabSelected = viewModel::onTabSelected,
                onSearchClick = {
                    viewModel.jumpToSearch(context)
                }
            )
            
            // Content：使用 Pager 支持左右滑动切换
            val pagerState = rememberPagerState(
                initialPage = viewModel.currentTab,
                pageCount = { 2 }
            )

            // 点击 Tab 时滚动到对应页
            LaunchedEffect(viewModel.currentTab) {
                Log.d("HomeView", "tab effect currentTab=${viewModel.currentTab} currentPage=${pagerState.currentPage}")
                if (pagerState.currentPage != viewModel.currentTab) {
                    pagerState.animateScrollToPage(viewModel.currentTab)
                }
            }

            // 滑动时更新 Tab 选中状态
            LaunchedEffect(pagerState.settledPage) {
                Log.d("HomeView", "settledPage effect settled=${pagerState.settledPage}")
                val settledPage = pagerState.settledPage
                if (viewModel.currentTab != settledPage) {
                    viewModel.onTabSelected(settledPage)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 0,
                pageContent = { page ->
                    when (page) {
                        0 -> RecommendView(
                            onHotPostsExploreMore = { viewModel.onTabSelected(1) },
                            onNavigateToSchool = onNavigateToSchool
                        )
                        1 -> CommunicateView(isActiveTab = viewModel.currentTab == 1)
//                        2 -> Text("工具箱", modifier = Modifier.fillMaxSize())
                        else -> RecommendView(onNavigateToSchool = onNavigateToSchool)
                    }
                }
            )
        }
}

@Composable
fun HomeViewPreview() {
    com.vortexa.ui.theme.BaseTheme { HomeView() }
}
