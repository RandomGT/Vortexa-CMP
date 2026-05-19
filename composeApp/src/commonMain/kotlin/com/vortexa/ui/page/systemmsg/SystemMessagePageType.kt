package com.vortexa.ui.page.systemmsg

/**
 * 系统消息页数据来源类型。
 *
 * [SYSTEM]：系统通知，请求 `/v/api/message/system`
 * [CLASSROOM_ASSISTANT]：课堂小助手，请求 `/v/api/message/classroom`
 */
object SystemMessagePageType {
    const val SYSTEM = 0
    const val CLASSROOM_ASSISTANT = 1
}
