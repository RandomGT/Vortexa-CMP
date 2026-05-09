package com.vortexa.ui.page.systemmsg

import androidx.compose.runtime.Composable
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.theme.BaseTheme

/**
 * 系统通知 / 课堂小助手消息列表页。通过 [EXTRA_MESSAGE_TYPE] 区分接口（默认 [SystemMessagePageType.SYSTEM]）。
 */
class SystemMessageActivity : BaseActivity() {

    companion object {
        const val EXTRA_MESSAGE_TYPE = "extra_system_message_type"
        /** 进入页时对会话标记已读（与 [EXTRA_MARK_READ_MESSAGE_ID] 成对，来自消息列表该行的 session） */
        const val EXTRA_MARK_READ_DIALOG_ID = "extra_mark_read_dialog_id"
        const val EXTRA_MARK_READ_MESSAGE_ID = "extra_mark_read_message_id"
    }

    @Composable
    override fun ContentPage() {
        val messageType = intent.getIntExtra(EXTRA_MESSAGE_TYPE, SystemMessagePageType.SYSTEM)
            .let { extra ->
                if (extra == SystemMessagePageType.CLASSROOM_ASSISTANT) {
                    SystemMessagePageType.CLASSROOM_ASSISTANT
                } else {
                    SystemMessagePageType.SYSTEM
                }
            }
        val markReadDialogId =
            if (intent.hasExtra(EXTRA_MARK_READ_DIALOG_ID)) intent.getLongExtra(EXTRA_MARK_READ_DIALOG_ID, 0L) else null
        val markReadMessageId =
            if (intent.hasExtra(EXTRA_MARK_READ_MESSAGE_ID)) intent.getLongExtra(EXTRA_MARK_READ_MESSAGE_ID, 0L) else null
        BaseTheme(belowStatusBar = true, aboveNavigationBar = true) {
            SystemMessageView(
                messageType = messageType,
                markReadDialogId = markReadDialogId?.takeIf { it > 0 },
                markReadMessageId = markReadMessageId?.takeIf { it > 0 },
                onBackClick = { finish() },
                onMenuClick = { /* 可扩展：弹出菜单 */ }
            )
        }
    }
}
