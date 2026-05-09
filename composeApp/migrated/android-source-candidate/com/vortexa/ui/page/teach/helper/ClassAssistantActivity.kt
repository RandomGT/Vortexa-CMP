package com.vortexa.ui.page.teach.helper

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.page.teach.order.one2one.OrderDetailActivity
import com.vortexa.ui.theme.BaseTheme

private const val INVALID_RESERVE_ID = -1

/**
 * 课堂小助手（Figma 504-57353）。
 * 数据来自 GET /v/api/c2c/teacher/reserve/classroom。
 * 学员/导师视角：可通过 Intent 传入 [ClassAssistantActivity.EXTRA_ROLE] 为 `teacher` 或 `student`（与 Scheme `role` 一致）；未传则按接口 [com.vortexa.model.ReserveClassroomDetail.counterpartRole] 推断（对方为导师则当前为学员，反之类推）。
 *
 * 导师：`待接受`（及同义旧文案）展示「拒绝 / 接受」；`即将开始`与`进行中`在开课前展示「私信 / 取消预约」（取消走拒绝接口）；已到开课时间仅「私信」；其余隐藏。
 * 学员：`待接受`/`即将开始`展示「取消预约」；`进行中`隐藏；`已完成`展示「重新预约」；已取消/已拒绝隐藏。
 * 非约定 status 文案时学员底部仍按课节时间兜底。
 */
class ClassAssistantActivity : BaseActivity() {

    @Composable
    override fun ContentPage() {
        val reserveId = intent.getIntExtra(EXTRA_RESERVE_ID, INVALID_RESERVE_ID)
        val roleOverride = ClassAssistantRoleScheme.parse(intent.getStringExtra(EXTRA_ROLE))
        if (reserveId == INVALID_RESERVE_ID) {
            LaunchedEffect(Unit) { finish() }
            return
        }
        // 必须带 key：默认 key 仅按 ViewModel 类型缓存，若首帧 role 与后续不一致会沿用错误实例
        val viewModelKey = "ClassAssistant|${reserveId}|${roleOverride?.name ?: "infer"}"
        val viewModel: ClassAssistantViewModel = viewModel(
            key = viewModelKey,
            factory = ClassAssistantViewModelFactory(reserveId, roleOverride)
        )
        LaunchedEffect(viewModel) {
            viewModel.navigateToOrderDetailAfterAccept.collect { id ->
                OrderDetailActivity.start(this@ClassAssistantActivity, id)
                finish()
            }
        }
        BaseTheme(belowStatusBar = true, aboveNavigationBar = true) {
            ClassAssistantView(
                viewModel = viewModel,
                onBackClick = { finish() },
                onClosedAfterCancel = { finish() }
            )
        }
    }

    companion object {
        const val EXTRA_RESERVE_ID = "class_assistant_reserve_id"
        const val EXTRA_ROLE = "class_assistant_role"

        /**
         * @param roleQuery Scheme：`teacher`（导师端）或 `student`（学员端）；null 或空白则进入页后按接口与登录用户推断。
         */
        @JvmStatic
        @JvmOverloads
        fun start(context: Context, reserveId: Int, roleQuery: String? = null) {
            val intent = Intent(context, ClassAssistantActivity::class.java).apply {
                putExtra(EXTRA_RESERVE_ID, reserveId)
                val normalized = roleQuery?.trim()?.lowercase().orEmpty()
                if (normalized.isNotEmpty()) {
                    putExtra(EXTRA_ROLE, normalized)
                }
            }
            context.startActivity(intent)
        }
    }
}
