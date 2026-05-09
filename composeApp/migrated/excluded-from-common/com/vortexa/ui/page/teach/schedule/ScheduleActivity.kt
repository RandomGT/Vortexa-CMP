package com.vortexa.ui.page.teach.schedule

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.theme.BaseTheme

/**
 * 日程/预约页。由调用方传入导师 ID，用于按日期查询该导师的预约时间。
 */
class ScheduleActivity : BaseActivity() {

    @Composable
    override fun ContentPage() {
        val teacherId = intent.getLongExtra(EXTRA_TEACHER_ID, INVALID_TEACHER_ID)
        BaseTheme(belowStatusBar = true, aboveNavigationBar = true) {
            ScheduleView(teacherId = teacherId)
        }
    }

    companion object {
        private const val EXTRA_TEACHER_ID = "extra_teacher_id"
        private const val INVALID_TEACHER_ID = -1L

        /** 启动日程页，传入导师 ID 用于查询预约时间 */
        fun start(context: Context, teacherId: Long) {
            context.startActivity(Intent(context, ScheduleActivity::class.java).apply {
                putExtra(EXTRA_TEACHER_ID, teacherId)
            })
        }
    }
}