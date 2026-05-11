package com.vortexa.net

import com.vortexa.model.SearchResultListItem
import com.vortexa.model.SearchResultRequest
import com.vortexa.model.SearchResultResponse
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

class SearchApi(
    private val client: ApiClient = ApiClient
) {
    suspend fun getSearchResult(
        pageNum: Int = 1,
        pageSize: Int = 4,
        request: SearchResultRequest
    ): SearchResultResponse {
        val response = client.postJson(
            PATH_SEARCH_RESULT,
            request.toJson(),
            query = pageQuery(pageNum, pageSize)
        )
        return response.dataObject().toSearchResultResponse(pageNum, pageSize)
    }

    companion object {
        private const val PATH_SEARCH_RESULT = "v/api/search/result"
    }
}

private fun pageQuery(pageNum: Int, pageSize: Int): Map<String, Any?> =
    mapOf("pageNum" to pageNum, "pageSize" to pageSize)

private fun SearchResultRequest.toJson(): JsonObject = buildJsonObject {
    put("keyword", keyword)
    put("type", type)
}

private fun ApiResponse.dataObject(): JsonObject =
    data as? JsonObject ?: throw ApiException(-1, "Response data is null")

private fun JsonElement.asObject(): JsonObject =
    this as? JsonObject ?: JsonObject(emptyMap())

private fun JsonObject.jsonArray(key: String): List<JsonElement> =
    (this[key] as? JsonArray)?.toList().orEmpty()

private fun JsonObject.long(key: String): Long? =
    (this[key] as? JsonPrimitive)?.longOrNull

private fun JsonObject.int(key: String): Int? =
    (this[key] as? JsonPrimitive)?.intOrNull

private fun JsonObject.boolean(key: String): Boolean? =
    (this[key] as? JsonPrimitive)?.booleanOrNull

private fun JsonObject.stringList(key: String): List<String>? {
    val array = this[key] as? JsonArray ?: return null
    return array.mapNotNull { (it as? JsonPrimitive)?.content }
}

private fun JsonObject.toSearchResultResponse(defaultPageNum: Int, defaultPageSize: Int): SearchResultResponse =
    SearchResultResponse(
        pageNum = int("pageNum") ?: defaultPageNum,
        pageSize = int("pageSize") ?: defaultPageSize,
        total = int("total") ?: jsonArray("list").size,
        list = jsonArray("list").map { it.asObject().toSearchResultListItem() }
    )

private fun JsonObject.toSearchResultListItem(): SearchResultListItem = SearchResultListItem(
    type = stringValue("type").orEmpty(),
    postId = long("postId") ?: long("id"),
    userId = long("userId"),
    nickname = stringValue("nickname") ?: stringValue("userName"),
    avatar = stringValue("avatar"),
    title = stringValue("title"),
    summary = stringValue("summary") ?: stringValue("content"),
    mediaList = stringList("mediaList"),
    totalMediaCount = int("totalMediaCount") ?: stringList("mediaList")?.size,
    module = stringValue("module") ?: stringValue("board"),
    likeCount = int("likeCount"),
    collectCount = int("collectCount"),
    replyCount = int("replyCount") ?: int("commentCount"),
    isLiked = boolean("isLiked") ?: false,
    isCollect = boolean("isCollect") ?: false,
    isInteractionHot = boolean("isInteractionHot") ?: false,
    isViewHot = boolean("isViewHot") ?: false,
    publishTime = stringValue("publishTime")
)
