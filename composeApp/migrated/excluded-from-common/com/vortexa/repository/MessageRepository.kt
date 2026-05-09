package com.vortexa.repository

import android.util.Log
import com.vortexa.api.MessageApi
import com.vortexa.lib_net.client.RetrofitClient
import com.vortexa.lib_net.exception.ApiException
import com.vortexa.model.DialogItem
import com.vortexa.model.MessageBatchReadRequest
import com.vortexa.model.MessageListResponse
import com.vortexa.model.SystemMessageListResponse
import com.vortexa.model.SystemNoticeItem

/**
 * 消息相关 Repository。
 * 负责对话框列表等接口调用与数据转换。
 *
 * @author LuXin
 */
class MessageRepository {

    private companion object {
        const val TAG = "MessageRepository"
        /** 与 [com.vortexa.ui.page.home.pager.message.MessageViewModel.CLASSROOM_ASSISTANT_USER_ID] 一致，用于会话列表里定位课堂小助手行日志 */
        private const val CLASSROOM_ASSISTANT_PEER_USER_ID = 1001L
        private const val CLASSROOM_ASSISTANT_DISPLAY_NAME = "课堂小助手"
    }

    /**
     * 打印 `/v/api/message/classroom` 返回的每一条，便于与后端联调核对。
     */
    private fun logClassroomMessagesResponse(source: String, data: SystemMessageListResponse) {
        val list = data.list
        Log.d(
            TAG,
            "[$source][课堂小助手/classroom] 响应概要 pageNum=${data.pageNum} pageSize=${data.pageSize} total=${data.total} listSize=${list.size}"
        )
        list.forEachIndexed { index, item ->
            Log.d(TAG, "[$source][课堂小助手/classroom] #$index ${formatSystemNoticeItemForLog(item)}")
        }
    }

    private fun formatSystemNoticeItemForLog(item: SystemNoticeItem): String {
        val card = item.card
        val cardSummary = when {
            card == null -> "card=null"
            else -> "card(type=${card.type} title=${card.title} url=${card.url} scheme=${card.scheme})"
        }
        val contentPreview = item.content.replace("\n", "\\n").take(200)
        return "noticeId=${item.noticeId} read=${item.read} time=${item.time} scheme=${item.scheme} $cardSummary contentPreview=\"$contentPreview\""
    }

    /** 会话列表里「课堂小助手」那一行的原始数据 */
    private fun logClassroomAssistantRowFromDialogList(source: String, index: Int, dialog: DialogItem) {
        val lm = dialog.lastMessage
        val contentPreview = (lm.content ?: "").replace("\n", "\\n").take(200)
        Log.d(
            TAG,
            "[$source][消息列表·课堂小助手行] index=$index dialogId=${dialog.dialogId} unreadCount=${dialog.unreadCount} " +
                "peerUserId=${dialog.userInfo.userId} peerName=${dialog.userInfo.userName} peerAvatar=${dialog.userInfo.userAvatar} " +
                "lastMessageId=${lm.messageId} type=${lm.messageType} sendTime=${lm.sendTime} media=${lm.media} content=\"$contentPreview\""
        )
    }

    private fun maybeLogClassroomRowsInMessageList(source: String, data: MessageListResponse) {
        val dialogs = data.dialogs ?: return
        dialogs.forEachIndexed { index, dialog ->
            val isClassroom =
                dialog.userInfo.userId == CLASSROOM_ASSISTANT_PEER_USER_ID ||
                    dialog.userInfo.userName == CLASSROOM_ASSISTANT_DISPLAY_NAME
            if (isClassroom) {
                logClassroomAssistantRowFromDialogList(source, index, dialog)
            }
        }
    }

    private val api: MessageApi by lazy {
        RetrofitClient.createService()
    }

    /**
     * 获取对话框列表。
     *
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @param userId 当前用户 ID，必填
     * @return 分页结果
     */
    suspend fun getMessageList(
        pageNum: Int = 1,
        pageSize: Int = 20,
        userId: Long
    ): Result<MessageListResponse> = runCatching {
        Log.d(TAG, "getMessageList: pageNum=$pageNum pageSize=$pageSize userId=$userId")
        val response = api.getMessageList(pageNum, pageSize, userId)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        val data = response.data ?: throw ApiException(-1, "Response data is null")
        maybeLogClassroomRowsInMessageList("getMessageList", data)
        data
    }

    /**
     * 获取系统通知列表。
     *
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @param userId 当前用户 ID，必填
     * @return 分页的系统通知列表
     */
    suspend fun getSystemMessages(
        pageNum: Int = 1,
        pageSize: Int = 20,
        userId: Long
    ): Result<SystemMessageListResponse> = runCatching {
        Log.d(TAG, "getSystemMessages: pageNum=$pageNum pageSize=$pageSize userId=$userId")
        val response = api.getSystemMessages(pageNum, pageSize, userId)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    /**
     * 获取课堂小助手消息列表（/v/api/message/classroom）。
     */
    suspend fun getClassroomMessages(
        pageNum: Int = 1,
        pageSize: Int = 20,
        userId: Long
    ): Result<SystemMessageListResponse> = runCatching {
        Log.d(TAG, "getClassroomMessages: pageNum=$pageNum pageSize=$pageSize userId=$userId")
        val response = api.getClassroomMessages(pageNum, pageSize, userId)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        val data = response.data ?: throw ApiException(-1, "Response data is null")
        logClassroomMessagesResponse("getClassroomMessages", data)
        data
    }

    /**
     * 批量标记已读。
     *
     * @param userId 当前用户 ID
     * @param dialogIds 对话框 ID，英文逗号分隔
     * @param messageIds 消息 ID，英文逗号分隔
     * @return 服务端标记成功的条数（若无 data 则 0）
     */
    suspend fun batchMarkRead(
        userId: Long,
        dialogIds: String,
        messageIds: String
    ): Result<Int> = runCatching {
        Log.d(TAG, "batchMarkRead: userId=$userId dialogIds=$dialogIds messageIds=$messageIds")
        val response = api.batchMarkRead(
            MessageBatchReadRequest(
                userId = userId,
                dialogIds = dialogIds,
                messageIds = messageIds
            )
        )
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: 0
    }
}
