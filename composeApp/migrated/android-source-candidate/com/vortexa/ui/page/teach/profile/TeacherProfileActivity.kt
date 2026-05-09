package com.vortexa.ui.page.teach.profile

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import com.vortexa.ui.base.BaseActivity

/**
 * 导师个人主页。
 * 由调用方传入导师 ID，通过 [start] 启动并请求 /v/api/c2c/teacher/detail。
 *
 * @author LuXin
 * @createTime 2026/2/25
 */
class TeacherProfileActivity : BaseActivity() {

    @Composable
    override fun ContentPage() {
        val teacherId = intent.getLongExtra(EXTRA_TEACHER_ID, INVALID_TEACHER_ID)
        TeacherProfileView(
            teacherId = teacherId,
            onBackClick = { finish() }
        )
    }

    companion object {
        private const val EXTRA_TEACHER_ID = "extra_teacher_id"
        private const val INVALID_TEACHER_ID = -1L

        /** 启动导师个人主页，传入导师 ID 用于请求详情接口 */
        fun start(context: Context, teacherId: Long) {
            context.startActivity(Intent(context, TeacherProfileActivity::class.java).apply {
                putExtra(EXTRA_TEACHER_ID, teacherId)
            })
        }
    }
}