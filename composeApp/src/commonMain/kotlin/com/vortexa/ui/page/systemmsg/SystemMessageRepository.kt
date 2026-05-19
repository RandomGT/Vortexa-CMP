package com.vortexa.ui.page.systemmsg

import com.vortexa.model.SystemMessageListResponse
import com.vortexa.model.SystemNoticeCard
import com.vortexa.model.SystemNoticeItem
import com.vortexa.net.ApiClient
import com.vortexa.net.ApiException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put

class SystemMessageRepository(
    private val client: ApiClient = ApiClient
) {
    suspend fun getSystemMessages(
        pageNum: Int = 1,
        pageSize: Int = 20,
        userId: Long
    ): Result<SystemMessageListResponse> = fetchMessages(
        path = "v/api/message/system",
        pageNum = pageNum,
        pageSize = pageSize,
        userId = userId
    )

    suspend fun getClassroomMessages(
        pageNum: Int = 1,
        pageSize: Int = 20,
        userId: Long
    ): Result<SystemMessageListResponse> = fetchMessages(
        path = "v/api/message/classroom",
        pageNum = pageNum,
        pageSize = pageSize,
        userId = userId
    )

    suspend fun batchMarkRead(
        userId: Long,
        dialogIds: String,
        messageIds: String
    ): Result<Unit> = runCatching {
        client.postJson(
            path = "v/api/message/read/batch",
            body = buildJsonObject {
                put("userId", userId)
                put("dialogIds", dialogIds)
                put("messageIds", messageIds)
            }
        )
    }

    private suspend fun fetchMessages(
        path: String,
        pageNum: Int,
        pageSize: Int,
        userId: Long
    ): Result<SystemMessageListResponse> = runCatching {
        val response = client.getJson(
            path = path,
            query = mapOf(
                "pageNum" to pageNum,
                "pageSize" to pageSize,
                "userId" to userId
            )
        )
        val data = response.data as? JsonObject ?: throw ApiException(-1, "Response data is null")
        data.toSystemMessageListResponse(pageNum, pageSize)
    }
}

private fun JsonObject.toSystemMessageListResponse(
    defaultPageNum: Int,
    defaultPageSize: Int
): SystemMessageListResponse {
    val items = array("list").mapNotNull { element ->
        (element as? JsonObject)?.toSystemNoticeItem()
    }
    return SystemMessageListResponse(
        pageNum = int("pageNum") ?: defaultPageNum,
        pageSize = int("pageSize") ?: defaultPageSize,
        total = int("total") ?: items.size,
        list = items
    )
}

private fun JsonObject.toSystemNoticeItem(): SystemNoticeItem {
    val cardObject = obj("card")
    val card = if (cardObject.isEmpty()) null else SystemNoticeCard(
        type = cardObject.string("type") ?: "link",
        title = cardObject.string("title") ?: "OK",
        url = cardObject.string("url"),
        scheme = cardObject.string("scheme")
    )
    return SystemNoticeItem(
        noticeId = int("noticeId") ?: 0,
        content = string("content") ?: "",
        card = card,
        time = string("time"),
        read = bool("read") ?: false,
        scheme = string("scheme")
    )
}

private fun JsonObject.array(key: String): List<kotlinx.serialization.json.JsonElement> =
    (this[key] as? JsonArray)?.toList().orEmpty()

private fun JsonObject.obj(key: String): JsonObject =
    this[key] as? JsonObject ?: JsonObject(emptyMap())

private fun JsonObject.string(key: String): String? =
    (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content

private fun JsonObject.int(key: String): Int? =
    (this[key] as? JsonPrimitive)?.intOrNull

private fun JsonObject.bool(key: String): Boolean? =
    (this[key] as? JsonPrimitive)?.booleanOrNull
