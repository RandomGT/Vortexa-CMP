package com.vortexa.ui.page.teach.schedule.confirm

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.page.teach.schedule.confirm2.Confirm2Activity
import com.vortexa.ui.page.teach.schedule.confirm2.Confirm2ViewModel
import com.vortexa.ui.page.teach.schedule.confirm2.Confirm2ViewModelFactory
import com.vortexa.ui.theme.BaseTheme

/**
 * 订单确认页（Figma 415-40747）。从日程页选择日期与时间槽后跳转，展示课程信息与预约详情，支持返回修改或确认订单。
 */
class ConfirmActivity : BaseActivity() {

    @Composable
    override fun ContentPage() {
        val teacherId = intent.getLongExtra(EXTRA_TEACHER_ID, -1L)
        val reserveDate = intent.getStringExtra(EXTRA_RESERVE_DATE).orEmpty()
        val reserveHour = intent.getStringExtra(EXTRA_RESERVE_HOUR).orEmpty()
        val viewModel: Confirm2ViewModel = viewModel(
            factory = Confirm2ViewModelFactory(teacherId, reserveDate, reserveHour)
        )
        val teacherName by viewModel.teacherDisplayName.collectAsState()
        val teacherAvatarUrl by viewModel.teacherAvatarUrl.collectAsState()
        val guideFee by viewModel.guideFeeText.collectAsState()
        val totalPoints by viewModel.totalPointsText.collectAsState()
        val displayName = teacherName.ifBlank {
            intent.getStringExtra(EXTRA_TEACHER_NAME).orEmpty().ifBlank { "…" }
        }
        BaseTheme(belowStatusBar = true, aboveNavigationBar = true) {
            ConfirmView(
                reserveDate = reserveDate,
                reserveHour = reserveHour,
                teacherName = displayName,
                teacherAvatarUrl = teacherAvatarUrl,
                orderPriceText = guideFee,
                payAmountText = totalPoints,
                onBackClick = { finish() },
                onModifyClick = { finish() },
                onConfirmClick = {
                    Confirm2Activity.start(
                        this@ConfirmActivity,
                        teacherId,
                        reserveDate,
                        reserveHour
                    )
                }
            )
        }
    }

    companion object {
        private const val EXTRA_TEACHER_ID = "extra_teacher_id"
        private const val EXTRA_RESERVE_DATE = "extra_reserve_date"
        private const val EXTRA_RESERVE_HOUR = "extra_reserve_hour"
        private const val EXTRA_COURSE_TITLE = "extra_course_title"
        private const val EXTRA_TEACHER_NAME = "extra_teacher_name"

        /**
         * 启动订单确认页。
         * @param courseTitle 预留参数，当前 UI 不展示课程标题
         * @param teacherName 导师姓名，接口未返回前用作占位
         */
        fun start(
            context: Context,
            teacherId: Long,
            reserveDate: String,
            reserveHour: String,
            courseTitle: String? = null,
            teacherName: String? = null
        ) {
            context.startActivity(Intent(context, ConfirmActivity::class.java).apply {
                putExtra(EXTRA_TEACHER_ID, teacherId)
                putExtra(EXTRA_RESERVE_DATE, reserveDate)
                putExtra(EXTRA_RESERVE_HOUR, reserveHour)
                putExtra(EXTRA_COURSE_TITLE, courseTitle)
                putExtra(EXTRA_TEACHER_NAME, teacherName)
            })
        }
    }
}
