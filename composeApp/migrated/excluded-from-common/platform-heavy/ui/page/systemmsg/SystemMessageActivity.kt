package com.vortexa.ui.page.systemmsg

class SystemMessageActivity {
    companion object {
        const val EXTRA_MESSAGE_TYPE: String = "message_type"
        const val EXTRA_MARK_READ_DIALOG_ID: String = "mark_read_dialog_id"
        const val EXTRA_MARK_READ_MESSAGE_ID: String = "mark_read_message_id"
    }
}

enum class SystemMessagePageType {
    SYSTEM,
    CLASSROOM_ASSISTANT,
}

