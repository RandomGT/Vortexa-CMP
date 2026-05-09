package com.vortexa.repository

import com.vortexa.model.DialogItem
import com.vortexa.model.DialogLastMessage
import com.vortexa.model.DialogUserInfo
import com.vortexa.model.MessageListResponse
import com.vortexa.net.ApiClient
import com.vortexa.net.ApiException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

class MessageRepository(
    private val client: ApiClient = ApiClient
) {
    suspend fun getMessageList(pageNum: Int = 1, pageSize: Int = 20, userId: Long): Result<MessageListResponse> =
        runCatching {
            val response = client.getJson(
                "v/api/message/list",
                mapOf("pageNum" to pageNum, "pageSize" to pageSize, "userId" to userId)
            )
            val data = response.data as? JsonObject ?: throw ApiException(-1, "Response data is null")
            MessageListResponse(
                pageNum = data.int("pageNum") ?: pageNum,
                pageSize = data.int("pageSize") ?: pageSize,
                total = data.int("total") ?: data.array("dialogs").size,
                dialogs = data.array("dialogs").map { it.asObject().toDialogItem() }
            )
        }

    suspend fun batchMarkRead(userId: Long, dialogIds: String, messageIds: String): Result<Unit> =
        runCatching {
            client.postJson(
                "v/api/message/read/batch",
                buildJsonObject {
                    put("userId", userId)
                    put("dialogIds", dialogIds)
                    put("messageIds", messageIds)
                }
            )
        }
}

private fun kotlinx.serialization.json.JsonElement.asObject(): JsonObject =
    this as? JsonObject ?: JsonObject(emptyMap())

private fun JsonObject.array(key: String): List<kotlinx.serialization.json.JsonElement> =
    (this[key] as? JsonArray)?.toList().orEmpty()

private fun JsonObject.obj(key: String): JsonObject =
    this[key] as? JsonObject ?: JsonObject(emptyMap())

private fun JsonObject.string(key: String): String? =
    (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content

private fun JsonObject.int(key: String): Int? =
    (this[key] as? JsonPrimitive)?.intOrNull

private fun JsonObject.long(key: String): Long? =
    (this[key] as? JsonPrimitive)?.longOrNull

private fun JsonObject.toDialogItem(): DialogItem = DialogItem(
    dialogId = int("dialogId") ?: 0,
    userInfo = obj("userInfo").let {
        DialogUserInfo(
            userId = it.long("userId") ?: 0L,
            userName = it.string("userName") ?: it.string("nickname") ?: "",
            userAvatar = it.string("userAvatar") ?: it.string("avatar")
        )
    },
    lastMessage = obj("lastMessage").let {
        DialogLastMessage(
            messageId = it.int("messageId") ?: 0,
            messageType = it.string("messageType") ?: "",
            content = it.string("content"),
            media = null,
            sendTime = it.string("sendTime") ?: ""
        )
    },
    unreadCount = int("unreadCount") ?: 0
)
