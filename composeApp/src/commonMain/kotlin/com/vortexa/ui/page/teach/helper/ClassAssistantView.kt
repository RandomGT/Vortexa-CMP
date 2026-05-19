package com.vortexa.ui.page.teach.helper

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
import androidx.compose.ui.platform.LocalContext
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.page.teach.order.one2one.CancelReserveConfirmModal
import com.vortexa.util.ToastUtil
import kotlinx.coroutines.delay

@Composable
fun ClassAssistantView(
    viewModel: ClassAssistantViewModel,
    onBackClick: () -> Unit,
    onClosedAfterCancel: () -> Unit = {},
    onAcceptedOpenOrderDetail: (reserveId: Int) -> Unit = {},
    onRebookClick: (teacherId: Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val ui by viewModel.ui.collectAsState()
    val pageStatus by viewModel.pageStatus.collectAsState()
    val showCancelConfirm by viewModel.showCancelConfirm.collectAsState()
    val showTutorRejectConfirm by viewModel.showTutorRejectConfirm.collectAsState()
    val cancelLoading by viewModel.cancelLoading.collectAsState()
    val cancelSuccess by viewModel.cancelSuccess.collectAsState()
    val tutorActionInProgress by viewModel.tutorActionInProgress.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(cancelSuccess) {
        if (cancelSuccess) {
            ToastUtil.show(context, "取消成功")
            viewModel.clearCancelSuccess()
            onClosedAfterCancel()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.navigateToOrderDetailAfterAccept.collect { reserveId ->
            onAcceptedOpenOrderDetail(reserveId)
        }
    }

    var timeTick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            timeTick++
        }
    }
    val now = remember(timeTick) { currentTeachingEpochMillis() }

    val bottomUi = remember(ui, now) {
        val state = ui ?: return@remember ClassAssistantBottomBarUi.Hidden
        classAssistantBottomBarUi(
            state = state,
            now = now,
            onMessage = { ToastUtil.show(context, "私信") },
            onReject = { viewModel.rejectReserve(reason = null) },
            onAccept = { viewModel.acceptReserve() },
            onCancelReserve = { viewModel.openCancelConfirm() },
            onTutorCancelBeforeCourse = { viewModel.openTutorRejectConfirm() },
            onRebook = {
                val tid = state.teacherIdForRebook
                if (tid > 0L) {
                    onRebookClick(tid)
                } else {
                    ToastUtil.show(context, "缺少导师信息，暂无法重新预约")
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        ui?.let { state ->
            ClassAssistantContent(
                ui = state,
                bottomUi = bottomUi,
                onBackClick = onBackClick,
                onSupportClick = { ToastUtil.show(context, "客服") },
                tutorActionsEnabled = !tutorActionInProgress
            )
        }
        PageStatusView(
            status = pageStatus,
            modifier = Modifier.fillMaxSize(),
            emptyMessage = "暂无课堂预约信息",
            showEmptyRefresh = true,
            onRefresh = { viewModel.loadDetail(showFullScreenLoading = true) }
        )
        if (showCancelConfirm) {
            CancelReserveConfirmModal(
                onDismiss = { viewModel.dismissCancelConfirm() },
                onConfirm = { viewModel.cancelReserve(reason = "用户取消") },
                cancelLoading = cancelLoading
            )
        }
        if (showTutorRejectConfirm) {
            CancelReserveConfirmModal(
                onDismiss = { viewModel.dismissTutorRejectConfirm() },
                onConfirm = { viewModel.confirmTutorRejectBeforeCourse() },
                cancelLoading = tutorActionInProgress
            )
        }
    }
}
