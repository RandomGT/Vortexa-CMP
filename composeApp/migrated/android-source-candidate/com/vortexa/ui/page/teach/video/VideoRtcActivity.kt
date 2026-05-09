package com.vortexa.ui.page.teach.video

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.compose.runtime.Composable
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.theme.BaseTheme

/**
 * 1对1 视频直播页，需传入 [channelName] 以请求声网 Token 并加入频道，并传入本节课的 [teacherId] 以正确展示导师/学员标签。
 *
 * @author LuXin
 * @createTime 2026/2/26
 */
class VideoRtcActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    @Composable
    override fun ContentPage() {
        val channelName = intent.getStringExtra(EXTRA_CHANNEL_NAME)
        if (channelName.isNullOrBlank()) {
            finish()
            return
        }
        val teacherId = intent.getLongExtra(EXTRA_TEACHER_ID, INVALID_TEACHER_ID)
        if (teacherId == INVALID_TEACHER_ID || teacherId <= 0L) {
            finish()
            return
        }
        val courseStartTimeMs = intent.getLongExtra(EXTRA_COURSE_START_MS, INVALID_COURSE_START_MS)
            .takeIf { it != INVALID_COURSE_START_MS }
        val courseEndTimeMs = intent.getLongExtra(EXTRA_COURSE_END_MS, INVALID_COURSE_END_MS)
            .takeIf { it != INVALID_COURSE_END_MS }
        BaseTheme(belowStatusBar = true, aboveNavigationBar = true) {
            VideoRtcView(
                channelName = channelName,
                courseTeacherId = teacherId,
                courseStartTimeMs = courseStartTimeMs,
                courseEndTimeMs = courseEndTimeMs
            )
        }
    }

    companion object {
        private const val EXTRA_CHANNEL_NAME = "channel_name"
        private const val EXTRA_TEACHER_ID = "teacher_id"
        private const val EXTRA_COURSE_START_MS = "course_start_ms"
        private const val EXTRA_COURSE_END_MS = "course_end_ms"
        private const val INVALID_TEACHER_ID = -1L
        /** 与有效时间戳区分，表示未传开课时间（顶栏沿用进入页面时刻正计时） */
        private const val INVALID_COURSE_START_MS = -1L
        /** 与有效时间戳区分，表示未传结课时间（不做下课提醒与强制关页） */
        private const val INVALID_COURSE_END_MS = -1L

        /** setResult 时附带：上一页（如订单详情）应重新拉取详情 */
        const val EXTRA_RESULT_REFRESH_PREVIOUS = "result_refresh_previous"

        fun newIntent(
            context: Context,
            channelName: String,
            teacherId: Long,
            courseStartTimeMs: Long? = null,
            courseEndTimeMs: Long? = null
        ): Intent = Intent(context, VideoRtcActivity::class.java).apply {
            putExtra(EXTRA_CHANNEL_NAME, channelName)
            putExtra(EXTRA_TEACHER_ID, teacherId)
            putExtra(EXTRA_COURSE_START_MS, courseStartTimeMs ?: INVALID_COURSE_START_MS)
            putExtra(EXTRA_COURSE_END_MS, courseEndTimeMs ?: INVALID_COURSE_END_MS)
        }

        /**
         * 启动视频直播页。
         * @param context 上下文
         * @param channelName 声网频道名，必填，用于请求 Token 并加入频道
         * @param teacherId 本订单/课程的导师 ID（与资料接口 [UserProfileInfo.teacherId] 比对），必填且须大于 0
         * @param courseStartTimeMs 课程开始时间（epoch毫秒），未到时间顶栏显示开始时间文案，到点后从该时刻正计时；为 null 时不传，顶栏从进入页时刻起计时
         * @param courseEndTimeMs 课程结束时间（epoch毫秒），有值时结束前 10 分钟弹窗、到点关闭页面；为 null 时不做结课逻辑
         */
        @JvmStatic
        @JvmOverloads
        fun start(
            context: Context,
            channelName: String,
            teacherId: Long,
            courseStartTimeMs: Long? = null,
            courseEndTimeMs: Long? = null
        ) {
            context.startActivity(newIntent(context, channelName, teacherId, courseStartTimeMs, courseEndTimeMs))
        }
    }
}