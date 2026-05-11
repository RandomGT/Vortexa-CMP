package com.vortexa.ui.page.profile.history

import com.vortexa.model.Post
import com.vortexa.model.ViewHistoryItem
import com.vortexa.model.ViewHistoryResponse
import com.vortexa.net.ApiClient
import com.vortexa.net.ApiException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

/**
 * 浏览记录 Repository。
 * 负责 GET /v/api/user/viewHistory 接口调用与数据转换。
 *
 * @author LuXin
 */
class HistoryRepository(
    private val client: ApiClient = ApiClient
) {

    /**
     * 获取浏览记录。
     *
     * @param module 板块名：null 全部，否则与筛选 chip 一致，如「杂谈」
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @return Result<ViewHistoryResponse> 成功返回分页结果
     */
    suspend fun getViewHistory(
        module: String? = null,
        pageNum: Int = 1,
        pageSize: Int = 20
    ): Result<ViewHistoryResponse> = runCatching {
        val response = client.getJson(
            path = "v/api/user/viewHistory",
            query = mapOf(
                "module" to module,
                "pageNum" to pageNum,
                "pageSize" to pageSize
            )
        )
        response.dataObject().toViewHistoryResponse(
            defaultPage = pageNum,
            defaultPageSize = pageSize
        )
    }

    /**
     * 将 ViewHistoryItem 映射为 Post，供 PostItem 展示。
     */
    fun mapToPost(item: ViewHistoryItem): Post {
        return Post(
            id = item.postId.toString(),
            username = item.nickname,
            avatar = item.avatar,
            time = item.publishTime ?: "",
            content = item.summary ?: "",
            images = item.mediaList ?: emptyList(),
            tagName = item.module,
            likeCount = item.likeCount,
            commentCount = item.replyCount,
            isLiked = item.isLiked,
            isCollect = item.isCollect,
            userId = item.userId,
            title = item.title,
            summary = item.summary,
            totalMediaCount = item.totalMediaCount,
            module = item.module,
            isInteractionHot = item.isInteractionHot,
            isViewHot = item.isViewHot,
            collectCount = item.collectCount,
            publishTime = item.publishTime,
            viewTime = item.viewTime
        )
    }
}

private fun com.vortexa.net.ApiResponse.dataObject(): JsonObject =
    data as? JsonObject ?: throw ApiException(-1, "Response data is null")

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

private fun JsonObject.boolean(key: String): Boolean? =
    (this[key] as? JsonPrimitive)?.booleanOrNull

private fun JsonObject.stringList(key: String): List<String>? =
    (this[key] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.content }

private fun JsonObject.toViewHistoryResponse(defaultPage: Int, defaultPageSize: Int): ViewHistoryResponse {
    val items = array("list").map { it.asObject().toViewHistoryItem() }
    return ViewHistoryResponse(
        total = int("total") ?: items.size,
        page = int("page") ?: int("pageNum") ?: defaultPage,
        pageSize = int("pageSize") ?: defaultPageSize,
        list = items
    )
}

private fun JsonObject.toViewHistoryItem(): ViewHistoryItem = ViewHistoryItem(
    type = string("type"),
    postId = long("postId") ?: long("id") ?: 0L,
    userId = long("userId") ?: long("authorId") ?: 0L,
    nickname = string("nickname") ?: string("userName") ?: string("authorName") ?: "",
    avatar = string("avatar") ?: string("userAvatar") ?: string("authorAvatar"),
    title = string("title"),
    summary = string("summary") ?: string("content"),
    mediaList = stringList("mediaList"),
    totalMediaCount = int("totalMediaCount") ?: stringList("mediaList")?.size ?: 0,
    module = string("module") ?: string("board"),
    isInteractionHot = boolean("isInteractionHot") ?: false,
    isViewHot = boolean("isViewHot") ?: false,
    likeCount = int("likeCount") ?: 0,
    collectCount = int("collectCount") ?: 0,
    replyCount = int("replyCount") ?: int("commentCount") ?: 0,
    isLiked = boolean("isLiked") ?: false,
    isCollect = boolean("isCollect") ?: false,
    publishTime = string("publishTime"),
    viewTime = string("viewTime")
)
