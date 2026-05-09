package com.vortexa.ui.page.teach.order.one2one

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.vortexa.ui.page.teach.video.VideoRtcActivity
import com.vortexa.util.ToastUtil
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.page.teach.profile.TeacherProfileActivity
import com.vortexa.ui.page.teach.schedule.ScheduleActivity
import com.vortexa.ui.theme.BaseTheme

/**
 * 一对一订单详情页 Activity（Figma 336-14162）。
 * 仅接收 [reserveId] 参数，进入页面后请求 /v/api/c2c/teacher/reserve/detail 并展示。
 */
class OrderDetailActivity : BaseActivity() {

    @Composable
    override fun ContentPage() {
        val reserveId = intent.getIntExtra(EXTRA_RESERVE_ID, INVALID_RESERVE_ID)
        if (reserveId == INVALID_RESERVE_ID) {
            finish()
            return
        }
        val viewModel: OrderDetailViewModel = viewModel(
            factory = OrderDetailViewModelFactory(reserveId)
        )
        val pageStatus by viewModel.pageStatus.collectAsState()
        val detailUi by viewModel.detailUi.collectAsState()
        val showCancelConfirm by viewModel.showCancelConfirm.collectAsState()
        val cancelLoading by viewModel.cancelLoading.collectAsState()
        val cancelSuccess by viewModel.cancelSuccess.collectAsState()
        val context = LocalContext.current
        val rtcLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val refresh = result.data?.getBooleanExtra(
                    VideoRtcActivity.EXTRA_RESULT_REFRESH_PREVIOUS,
                    false
                ) == true
                if (refresh) viewModel.loadDetail()
            }
        }
        // 每分钟触发一次重组，使「进入课程」可进入状态随时间更新
        var enterCheckTick by remember { mutableStateOf(0) }
        LaunchedEffect(Unit) {
            while (true) {
                delay(60_000)
                enterCheckTick++
            }
        }
        LaunchedEffect(cancelSuccess) {
            if (cancelSuccess) {
                Toast.makeText(context, "取消成功", Toast.LENGTH_SHORT).show()
                viewModel.clearCancelSuccess()
                finish()
            }
        }

        BaseTheme(belowStatusBar = true, aboveNavigationBar = true) {
            Box(Modifier.fillMaxSize()) {
                detailUi?.let { ui ->
                    OrderDetailView(
                        ui = ui,
                        timeTick = enterCheckTick,
                        onResumeRefresh = { viewModel.loadDetail() },
                        onBackClick = { finish() },
                        onTeacherProfileClick = {
                            if (ui.teacherId > 0L) {
                                TeacherProfileActivity.start(context, ui.teacherId)
                            } else {
                                ToastUtil.show(context, "导师信息异常")
                            }
                        },
                        onEnterCourseClick = {
//                            if (!ui.canEnterCourseRoom()) {
//                                ToastUtil.show(context, "开始前10分钟可进入课堂")
//                                return@OrderDetailView
//                            }
                            val channel = ui.channelName
                            if (channel.isNullOrBlank()) {
                                ToastUtil.show(context, "无法进入课程，缺少频道信息")
                            } else if (ui.teacherId <= 0L) {
                                ToastUtil.show(context, "无法进入课程，缺少导师信息")
                            } else {
                                rtcLauncher.launch(
                                    VideoRtcActivity.newIntent(
                                        context,
                                        channel,
                                        ui.teacherId,
                                        ui.courseStartEpochMilli(),
                                        ui.courseEndEpochMilli()
                                    )
                                )
                            }
                        },
                        onPrimaryClick = {
                            if (ui.isPending) viewModel.openCancelConfirm()
                            else {
                                if (ui.teacherId <= 0L) {
                                    Log.w(TAG, "rebook blocked, invalid teacherId=${ui.teacherId}, reserveId=$reserveId")
                                    ToastUtil.show(context, "导师信息异常，暂无法再次预约")
                                    return@OrderDetailView
                                }
                                Log.i(TAG, "rebook start schedule flow, teacherId=${ui.teacherId}, reserveId=$reserveId")
                                ScheduleActivity.start(context, ui.teacherId)
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

    companion object {
        private const val TAG = "OrderDetailActivity"
        private const val EXTRA_RESERVE_ID = "reserve_id"
        private const val INVALID_RESERVE_ID = -1

        /**
         * 启动订单详情页。
         * @param context 上下文
         * @param reserveId 预约 ID，必填
         */
        @JvmStatic
        fun start(context: Context, reserveId: Int) {
            val intent = Intent(context, OrderDetailActivity::class.java).apply {
                putExtra(EXTRA_RESERVE_ID, reserveId)
            }
            context.startActivity(intent)
        }
    }
}
