package com.vortexa.repository

import com.vortexa.model.InteractionListItem
import com.vortexa.model.InteractionRequest
import com.vortexa.model.InteractionResponse
import com.vortexa.net.ApiClient
import com.vortexa.net.ApiException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

/**
 * 互动管理数据仓库。
 * 负责 /v/api/user/interactions 接口调用与数据转换。
 */
class InteractionRepository(
    private val client: ApiClient = ApiClient
) {
    /**
     * 获取互动列表。
     *
     * @param actorType 互动对象：0=所有人，1=我的关注
     * @param actionType 互动类型：0=点赞，1=回复
     * @param direction 互动方向：0=全部，1=我发起的，2=被互动的
     * @param pageNum 页码
     * @param pageSize 每页条数
     */
    suspend fun getInteractions(
        actorType: Int = 0,
        actionType: Int = 0,
        direction: Int = 0,
        pageNum: Int = 1,
        pageSize: Int = 20,
    ): Result<InteractionResponse> = runCatching {
        val request = InteractionRequest(
            actorType = actorType,
            actionType = actionType,
            direction = direction
        )
        val response = client.postJson(
            path = "v/api/user/interactions",
            body = request.toJson(),
            query = mapOf("pageNum" to pageNum, "pageSize" to pageSize)
        )
        val data = response.data as? JsonObject ?: throw ApiException(-1, "Response data is null")
        data.toInteractionResponse(defaultPage = pageNum, defaultPageSize = pageSize)
    }
}

private fun InteractionRequest.toJson(): JsonObject = buildJsonObject {
    put("actorType", actorType)
    put("actionType", actionType)
    put("direction", direction)
}

private fun JsonElement.asObject(): JsonObject =
    this as? JsonObject ?: JsonObject(emptyMap())

private fun JsonObject.array(key: String): List<JsonElement> =
    (this[key] as? JsonArray)?.toList().orEmpty()

private fun JsonObject.string(key: String): String? =
    (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content

private fun JsonObject.int(key: String): Int? =
    (this[key] as? JsonPrimitive)?.intOrNull

private fun JsonObject.long(key: String): Long? =
    (this[key] as? JsonPrimitive)?.longOrNull

private fun JsonObject.toInteractionResponse(defaultPage: Int, defaultPageSize: Int): InteractionResponse =
    InteractionResponse(
        total = int("total") ?: array("list").size,
        page = int("page") ?: int("pageNum") ?: defaultPage,
        pageSize = int("pageSize") ?: defaultPageSize,
        list = array("list").map { it.asObject().toInteractionListItem() }
    )

private fun JsonObject.toInteractionListItem(): InteractionListItem = InteractionListItem(
    userId = long("userId") ?: long("actorUserId") ?: long("targetUserId") ?: 0L,
    userName = string("userName") ?: string("nickname") ?: string("actorUserName") ?: string("targetUserName") ?: "",
    userAvatar = string("userAvatar") ?: string("avatar") ?: string("actorUserAvatar") ?: string("targetUserAvatar") ?: "",
    action = int("action") ?: int("actionType") ?: 0,
    type = int("type") ?: int("targetType") ?: 0,
    typeData = string("typeData") ?: string("targetData") ?: string("content") ?: string("title") ?: "",
    time = string("time") ?: string("createTime") ?: string("publishTime") ?: "",
    postId = long("postId") ?: long("targetId") ?: 0L
)
