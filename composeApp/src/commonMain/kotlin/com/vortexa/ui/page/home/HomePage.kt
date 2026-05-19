package com.vortexa.ui.page.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import android.content.Context
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.ui.component.HomeTabContent
import com.vortexa.ui.page.home.pager.FavoriteView
import com.vortexa.ui.page.home.pager.message.MessageView
import com.vortexa.ui.page.home.pager.message.SystemMessageEntry
import com.vortexa.ui.page.home.pager.message.MessageViewModel
import com.vortexa.ui.page.home.pager.profile.ProfileView
import com.vortexa.ui.page.home.pager.school.SchoolView
import com.vortexa.ui.page.home.pager.home.HomeView
import com.vortexa.ui.theme.BaseTheme

/**
 *  desc : 首页，5个Tab
 *
 *
 *  @author LuXin
 *  @createTime 2026/1/19
 */
@Composable
fun HomePage(
    initialTab: Int = 0,
    onOpenSystemMessage: (SystemMessageEntry) -> Unit = {},
    onOpenMyClass: () -> Unit = {},
    onOpenWallet: () -> Unit = {},
    onOpenCreatorCenter: () -> Unit = {},
    onOpenPaperManagement: () -> Unit = {},
    onOpenMyFocus: () -> Unit = {},
    onOpenTeacherProfile: (Long) -> Unit = {},
    onOpenSchedule: (Long) -> Unit = {},
    onOpenOtherUserProfile: (Long) -> Unit = {},
) {
    Log.d("HomePage", "compose start")
    val context = Context()
    val safeInitialTab = initialTab.coerceIn(0, 4)
    val viewModel = vortexaViewModel { HomeViewModel() }
    val messageViewModel = vortexaViewModel { MessageViewModel() }
    val rememberTabIndex by remember { viewModel.currentTab }
    val hasMessageUnread by messageViewModel.hasUnreadDialogs.collectAsState()
    val pageState = rememberPagerState(initialPage = safeInitialTab, pageCount = { 5 })
    LaunchedEffect(safeInitialTab) {
        if (rememberTabIndex != safeInitialTab) {
            viewModel.onTabClick(safeInitialTab)
        }
    }
    LaunchedEffect(rememberTabIndex) {
        Log.d("HomePage", "LaunchedEffect rememberTabIndex=$rememberTabIndex currentPage=${pageState.currentPage}")
        if (pageState.currentPage != rememberTabIndex) {
            pageState.animateScrollToPage(rememberTabIndex)
        }
    }
    // 根据当前 Tab 动态控制状态栏图标颜色：Profile(索引4)需浅色图标，其余为深色
    BaseTheme(
        statusBarTextDark = pageState.settledPage != 4,
        belowStatusBar = false
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier.fillMaxWidth()
                    .weight(1f)
            ) {
                HorizontalPager(
                    state = pageState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 0,
                    userScrollEnabled = false,
                    pageContent = { page ->
                    when(page){
                        0 -> HomeView(
                            onNavigateToSchool = {
                                pageState.requestScrollToPage(2)
                                viewModel.onTabClick(2)
                            }
                        )
                        1 -> MessageView(
                            viewModel = messageViewModel,
                            isSelected = rememberTabIndex == 1,
                            onSystemMessageClick = onOpenSystemMessage,
                        )
                        2 -> SchoolView()
                        3 -> FavoriteView(
                            id = page,
                            isSelected = rememberTabIndex == 3
                        )
                        4 -> ProfileView(
                            isSelected = rememberTabIndex == 4,
                            onWalletClick = onOpenWallet,
                            onCourseClick = onOpenMyClass,
                            onCreatorClick = onOpenCreatorCenter,
                            onPaperManagementClick = onOpenPaperManagement,
                            onFocusClick = onOpenMyFocus,
                        )
                        else -> {HomeView()}
                    }
                }
            )
            }
            HomeTabContent(
                modifier = Modifier.fillMaxWidth()
                    .height(50.dp)
                    .background(Color.White),
                selected = rememberTabIndex,
                messageTip = if (hasMessageUnread) listOf(1) else emptyList()
            ) { tabIndex ->
                if (HomeGuestTabLogin.openGuestLoginInsteadOfTab(context, tabIndex)) {
                    return@HomeTabContent
                }
                pageState.requestScrollToPage(tabIndex)
                viewModel.onTabClick(tabIndex)
            }
        }
    }
}

@Composable
fun HomePagePreview(){
    HomePage()
}
