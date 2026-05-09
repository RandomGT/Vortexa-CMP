package com.vortexa.ui.page.teach.schedule.confirm2

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.page.home.HomeActivity
import com.vortexa.ui.page.teach.order.one2one.OrderDetailActivity
import com.vortexa.ui.theme.BaseTheme

/**
 * 支付确认页（Figma 422-36569）。从订单确认页点击「确认订单」跳转，展示积分费用与优惠；点击「确认支付」调用预约接口；成功后清空栈（仅保留首页）并跳转订单详情。
 */
class Confirm2Activity : BaseActivity() {

    @Composable
    override fun ContentPage() {
        val teacherId = intent.getLongExtra(EXTRA_TEACHER_ID, -1L)
        val reserveDate = intent.getStringExtra(EXTRA_RESERVE_DATE).orEmpty()
        val reserveHour = intent.getStringExtra(EXTRA_RESERVE_HOUR).orEmpty()
        val viewModel: Confirm2ViewModel = viewModel(
            factory = Confirm2ViewModelFactory(teacherId, reserveDate, reserveHour)
        )
        val payLoading by viewModel.payLoading.collectAsState()
        val reserveSuccessReserveId by viewModel.reserveSuccessReserveId.collectAsState()
        val teacherName by viewModel.teacherDisplayName.collectAsState()
        val teacherAvatarUrl by viewModel.teacherAvatarUrl.collectAsState()
        val guideFee by viewModel.guideFeeText.collectAsState()
        val balancePoints by viewModel.balancePointsText.collectAsState()
        val totalPoints by viewModel.totalPointsText.collectAsState()
        val context = LocalContext.current
        val durationText = parseDurationFromSlot(reserveHour)
        val courseStartTime = formatCourseStartTime(reserveDate, reserveHour)

        LaunchedEffect(reserveSuccessReserveId) {
            val reserveId = reserveSuccessReserveId ?: return@LaunchedEffect
            val homeIntent = Intent(context, HomeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(homeIntent)
            OrderDetailActivity.start(context, reserveId.toInt())
            viewModel.clearReserveSuccess()
            finish()
        }

        BaseTheme(belowStatusBar = true, aboveNavigationBar = true) {
            PayConfirmView(
                teacherName = teacherName.ifBlank { "…" },
                teacherAvatarUrl = teacherAvatarUrl,
                courseStartTime = courseStartTime,
                durationText = durationText,
                guideFee = guideFee,
                balancePoints = balancePoints,
                totalPoints = totalPoints,
                payLoading = payLoading,
                onBackClick = { finish() },
                onPayClick = { viewModel.reserve() }
            )
        }
    }

    /** 与日程/订单确认页一致：yyyy-MM-dd + 时段起始时刻，如 2025-10-08 18:00 */
    private fun formatCourseStartTime(reserveDate: String, reserveHour: String): String {
        if (reserveDate.isEmpty() || reserveHour.isEmpty()) return "—"
        val start = reserveHour.substringBefore("-").trim()
        return "${reserveDate.replace("/", "-")} $start"
    }

    private fun parseDurationFromSlot(reserveHour: String): String {
        if (reserveHour.isEmpty()) return "2小时"
        val parts = reserveHour.split("-")
        if (parts.size != 2) return "1小时"
        val start = parts[0].trim().substringBefore(":")
        val end = parts[1].trim().substringBefore(":")
        val startH = start.toIntOrNull() ?: 0
        val endH = end.toIntOrNull() ?: 0
        val hours = (endH - startH).coerceAtLeast(1)
        return "${hours}小时"
    }

    companion object {
        private const val EXTRA_TEACHER_ID = "extra_teacher_id"
        private const val EXTRA_RESERVE_DATE = "extra_reserve_date"
        private const val EXTRA_RESERVE_HOUR = "extra_reserve_hour"
        private const val EXTRA_COURSE_TITLE = "extra_course_title"
        private const val EXTRA_TEACHER_NAME = "extra_teacher_name"

        /** 从订单确认页启动支付确认页，传入预约参数与课程/导师信息。 */
        fun start(
            context: Context,
            teacherId: Long,
            reserveDate: String,
            reserveHour: String,
            courseTitle: String? = null,
            teacherName: String? = null
        ) {
            context.startActivity(Intent(context, Confirm2Activity::class.java).apply {
                putExtra(EXTRA_TEACHER_ID, teacherId)
                putExtra(EXTRA_RESERVE_DATE, reserveDate)
                putExtra(EXTRA_RESERVE_HOUR, reserveHour)
                putExtra(EXTRA_COURSE_TITLE, courseTitle)
                putExtra(EXTRA_TEACHER_NAME, teacherName)
            })
        }
    }
}
