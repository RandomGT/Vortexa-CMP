package com.vortexa.repository

import com.vortexa.model.MessageListResponse

class MessageRepository {
    suspend fun getMessageList(pageNum: Int = 1, pageSize: Int = 20, userId: Long): Result<MessageListResponse> =
        Result.success(MessageListResponse(pageNum, pageSize, 0, emptyList()))

    suspend fun batchMarkRead(userId: Long, dialogIds: String, messageIds: String): Result<Unit> =
        Result.success(Unit)
}

