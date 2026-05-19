package com.vortexa.ui.page.teach.myclass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.config.UserConfig
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.page.teach.myclass.one2one.MyClassOneToOnePage
import com.vortexa.ui.page.teach.myclass.school.MyClassSchoolPage
import com.vortexa.ui.viewmodel.vortexaViewModel
import kotlinx.coroutines.launch

/**
 * 我的课程页：Figma TitleBar + TabBar（一对一服务 / 涡联学院）+ HorizontalPager。
 *
 * @param onBackClick 点击 TitleBar 返回按钮时回调，通常 finish Activity
 */
@Composable
fun MyClassView(
    onBackClick: () -> Unit = {},
    onOpenClassAssistant: (reserveId: Int, roleQuery: String?) -> Unit = { _, _ -> },
    onOpenOrderDetail: (reserveId: Int) -> Unit = {},
    viewModel: MyClassViewModel = vortexaViewModel { MyClassViewModel() }
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex,
        pageCount = { MyClassTabs.size }
    )
    val scope = rememberCoroutineScope()
    val one2OneList by viewModel.one2OneList.collectAsState()
    val one2OneFilterIndex by viewModel.one2OneFilterIndex.collectAsState()
    val one2OnePageStatus by viewModel.one2OnePageStatus.collectAsState()

    LaunchedEffect(pagerState.settledPage) {
        selectedTabIndex = pagerState.settledPage
    }

    LaunchedEffect(Unit) {
        viewModel.loadReserveList(RESERVE_LIST_TYPE_TEACHER)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MyClassTitleBar(onBackClick = onBackClick)

        Box(
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            MyClassTabBar(
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
            beyondViewportPageCount = MyClassTabs.size,
            pageContent = { page ->
                when (page) {
                    0 -> Box(Modifier.fillMaxSize()) {
                        MyClassOneToOnePage(
                            items = one2OneList,
                            selectedFilterIndex = one2OneFilterIndex,
                            onFilterIndexChange = viewModel::setOne2OneFilterIndex,
                            onItemMoreClick = { },
                            onItemButtonClick = { index ->
                                one2OneList.getOrNull(index)?.let { item ->
                                    val myTeacherId = UserConfig.getTeacherId()
                                    val openClassAssistant = item.status == "待接受" &&
                                        item.teacherId > 0L &&
                                        myTeacherId > 0L &&
                                        item.teacherId == myTeacherId
                                    if (openClassAssistant) {
                                        onOpenClassAssistant(item.reserveId.toInt(), "teacher")
                                    } else {
                                        onOpenOrderDetail(item.reserveId.toInt())
                                    }
                                }
                            }
                        )
                        PageStatusView(
                            status = one2OnePageStatus,
                            modifier = Modifier.fillMaxSize(),
                            onRefresh = { viewModel.loadReserveList(RESERVE_LIST_TYPE_TEACHER) }
                        )
                    }
                    1 -> MyClassSchoolPage(viewModel = viewModel)
                    else -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = MyClassTabs.getOrElse(page) { "" },
                            color = Color.Gray
                        )
                    }
                }
            }
        )
    }
}

@Composable
@Preview
fun MyClassPreview() {
    MyClassView()
}
