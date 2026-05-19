package com.vortexa.ui.page.systemmsg

/**
 * 兼容 Home message 入口传参的常量容器。
 *
 * 这里保留旧命名，实际页面入口已迁移为 [SystemMessageView] composable callbacks。
 */
class SystemMessageActivity {
    companion object {
        const val EXTRA_MESSAGE_TYPE: String = "extra_system_message_type"
        const val EXTRA_MARK_READ_DIALOG_ID: String = "extra_mark_read_dialog_id"
        const val EXTRA_MARK_READ_MESSAGE_ID: String = "extra_mark_read_message_id"
    }
}
