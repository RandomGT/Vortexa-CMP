package com.vortexa.model

/**
 * 系统通知列表接口响应体。
 *
 * @see com.vortexa.api.MessageApi.getSystemMessages
 */
data class SystemMessageListResponse(
    val pageNum: Int,
    val pageSize: Int,
    val total: Int,
    val list: List<SystemNoticeItem>
)

/**
 * 单条系统通知。
 *
 * @param noticeId 通知 ID
 * @param content 通知正文
 * @param card 关联卡片（如查看帖子链接），可为 null
 * @param time 时间，格式如 2026-01-21 09:12:33
 * @param read 是否已读
 * @param scheme 端内跳转用完整 URI（如 `vortexa://post/detail?postId=1`），与 [card] 内 [SystemNoticeCard.scheme] 二选一由后端下发
 */
data class SystemNoticeItem(
    val noticeId: Int,
    val content: String,
    val card: SystemNoticeCard?,
    val time: String?,
    val read: Boolean = false,
    val scheme: String? = null
)

/**
 * 系统通知关联卡片。
 *
 * @param type 类型，如 link
 * @param title 卡片标题，如「查看帖子」
 * @param url 跳转路径，如 /post/101
 * @param scheme 端内跳转完整 URI，优先于服务端仅下发 path 时的本地拼装（与条目级 [SystemNoticeItem.scheme] 择一）
 */
data class SystemNoticeCard(
    val type: String,
    val title: String,
    val url: String? = null,
    val scheme: String? = null
)

/**
 * 批量标记已读请求体。
 *
 * @see com.vortexa.api.MessageApi.batchMarkRead
 *
 * @param userId 当前用户 ID
 * @param dialogIds 对话框 ID 列表，英文逗号分隔
 * @param messageIds 消息 ID 列表，英文逗号分隔
 */
data class MessageBatchReadRequest(
    val userId: Long,
    val dialogIds: String,
    val messageIds: String
)

/**
 * 对话框列表接口响应体。
 *
 * @see com.vortexa.api.MessageApi.getMessageList
 */
data class MessageListResponse(
    val pageNum: Int,
    val pageSize: Int,
    val total: Int,
    val dialogs: List<DialogItem>
)

/**
 * 单条对话框数据。
 *
 * @param dialogId 对话 ID
 * @param userInfo 对端用户信息（系统通知或私聊对象）
 * @param lastMessage 最后一条消息
 * @param unreadCount 未读数量
 */
data class DialogItem(
    val dialogId: Int,
    val userInfo: DialogUserInfo,
    val lastMessage: DialogLastMessage,
    val unreadCount: Int
)

/**
 * 对话框中用户信息。
 *
 * @param userId 用户 ID
 * @param userName 用户昵称
 * @param userAvatar 头像 URL，系统通知时为占位文案如「客户端默认头像」
 */
data class DialogUserInfo(
    val userId: Long,
    val userName: String,
    val userAvatar: String?
)

/**
 * 对话框中最后一条消息。
 *
 * @param messageId 消息 ID
 * @param messageType 消息类型，如 text
 * @param content 文本内容
 * @param media 媒体信息，文本消息为 null
 * @param sendTime 发送时间，格式如 2026-01-21 10:15:22
 */
data class DialogLastMessage(
    val messageId: Int,
    val messageType: String,
    val content: String?,
    val media: Any?,
    val sendTime: String
)
