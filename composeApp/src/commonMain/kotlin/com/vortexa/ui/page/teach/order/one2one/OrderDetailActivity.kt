package com.vortexa.ui.page.teach.order.one2one

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.vortexa.navigation.AppRoute
import com.vortexa.navigation.NavigationRouteBridge
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.theme.BaseTheme
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.util.ToastUtil
import kotlinx.coroutines.delay

object OrderDetailActivity {
    const val EXTRA_RESERVE_ID: String = "reserve_id"

    fun start(context: Any?, reserveId: Int) {
        NavigationRouteBridge.navigate(AppRoute.OrderDetail(reserveId))
    }
}

@Composable
fun OrderDetailRoute(
    reserveId: Int,
    onBackClick: () -> Unit = {},
    onTeacherProfileClick: (teacherId: Long) -> Unit = {},
    onRebookClick: (teacherId: Long) -> Unit = {},
    onCourseEntryClick: (OrderDetailUi) -> Unit = {},
    onClosedAfterCancel: () -> Unit = {},
    viewModel: OrderDetailViewModel = vortexaViewModel(key = "order-detail-$reserveId") {
        OrderDetailViewModel(reserveId)
    }
) {
    val pageStatus by viewModel.pageStatus.collectAsState()
    val detailUi by viewModel.detailUi.collectAsState()
    val showCancelConfirm by viewModel.showCancelConfirm.collectAsState()
    val cancelLoading by viewModel.cancelLoading.collectAsState()
    val cancelSuccess by viewModel.cancelSuccess.collectAsState()
    var enterCheckTick by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            enterCheckTick++
        }
    }
    LaunchedEffect(cancelSuccess) {
        if (cancelSuccess) {
            ToastUtil.show("取消成功")
            viewModel.clearCancelSuccess()
            onClosedAfterCancel()
        }
    }

    BaseTheme(belowStatusBar = true, aboveNavigationBar = true) {
        Box(Modifier.fillMaxSize()) {
            detailUi?.let { ui ->
                OrderDetailView(
                    ui = ui,
                    timeTick = enterCheckTick,
                    onResumeRefresh = { viewModel.loadDetail() },
                    onBackClick = onBackClick,
                    onTeacherProfileClick = {
                        if (ui.teacherId > 0L) onTeacherProfileClick(ui.teacherId)
                        else ToastUtil.show("导师信息异常")
                    },
                    onEnterCourseClick = {
                        if (ui.channelName.isNullOrBlank()) {
                            ToastUtil.show("无法进入课程，缺少频道信息")
                        } else if (ui.teacherId <= 0L) {
                            ToastUtil.show("无法进入课程，缺少导师信息")
                        } else if (!ui.canEnterCourseRoom()) {
                            ToastUtil.show("课程未开始，暂不能进入")
                        } else {
                            onCourseEntryClick(ui)
                        }
                    },
                    onPrimaryClick = {
                        if (ui.isPending) {
                            viewModel.openCancelConfirm()
                        } else if (ui.teacherId > 0L) {
                            onRebookClick(ui.teacherId)
                        } else {
                            ToastUtil.show("导师信息异常，暂无法再次预约")
                        }
                    }
                )
            }
            PageStatusView(
                status = pageStatus,
                modifier = Modifier.fillMaxSize(),
                onRefresh = { viewModel.loadDetail() }
            )
            if (showCancelConfirm) {
                CancelReserveConfirmModal(
                    onDismiss = { viewModel.dismissCancelConfirm() },
                    onConfirm = { viewModel.cancelReserve(reason = "用户取消") },
                    cancelLoading = cancelLoading
                )
            }
        }
    }
}
