package com.vortexa.ui.page.profile.collection

import android.util.Log
import com.vortexa.model.CollectionItem
import com.vortexa.model.CollectionRequest
import com.vortexa.model.CollectionResponse
import com.vortexa.model.Post
import com.vortexa.net.ApiClient
import com.vortexa.net.ApiException
import com.vortexa.net.ApiResponse
import com.vortexa.net.stringValue
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

/**
 * 收藏列表 Repository。
 * 负责 POST /v/api/user/collections 接口调用与数据转换（module 在 body）。
 *
 * @author LuXin
 */
class CollectionRepository {

    private val client: ApiClient = ApiClient

    /**
     * 获取收藏列表。
     *
     * @param module 板块中文名（如「杂谈」）；传 null 表示全部
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @return Result<CollectionResponse> 成功返回分页结果
     */
    suspend fun getCollections(
        module: String? = null,
        pageNum: Int = 1,
        pageSize: Int = 20
    ): Result<CollectionResponse> = runCatching {
        Log.d(TAG, "getCollections: module=$module, pageNum=$pageNum, pageSize=$pageSize")
        val response = client.postJson(
            path = PATH_COLLECTIONS,
            query = pageQuery(pageNum, pageSize),
            body = buildJsonObject {
                if (module != null) {
                    put("module", module)
                }
            }
        )
        response.dataObject().toCollectionResponse(pageNum, pageSize)
    }

    /**
     * 将 CollectionItem 映射为 Post，供 PostItem 展示。
     */
    fun mapToPost(item: CollectionItem): Post {
        val published = item.publishTime?.trim().orEmpty()
        return Post(
            id = item.postId.toString(),
            username = item.nickname,
            avatar = item.authorAvatar,
            time = published,
            content = item.summary,
            title = item.title,
            publishTime = item.publishTime,
            likeCount = item.likeCount,
            commentCount = item.replyCount,
            isLiked = item.isLiked,
            isCollect = true,
            userId = item.authorId,
            module = item.module,
            collectCount = item.collectCount,
            images = item.mediaList?:emptyList()
        )
    }

    companion object {
        private const val TAG = "CollectionRepository"
        private const val PATH_COLLECTIONS = "v/api/user/collections"

        /**
         * 收藏页筛选索引与接口 [CollectionRequest.module] 的对应关系（与 [CollectionFilter] Chip 顺序一致）。
         */
        fun moduleParamForFilterIndex(filterIndex: Int): String? = when (filterIndex) {
            1 -> "杂谈"
            2 -> "交易经验"
            3 -> "玩法"
            else -> null
        }
    }
}

private fun pageQuery(pageNum: Int, pageSize: Int): Map<String, Any?> =
    mapOf("pageNum" to pageNum, "pageSize" to pageSize)

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

private fun JsonObject.stringList(key: String): List<String>? =
    (this[key] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.content }

private fun JsonObject.toCollectionResponse(defaultPageNum: Int, defaultPageSize: Int): CollectionResponse =
    CollectionResponse(
        total = int("total") ?: jsonArray("list").size,
        pageNum = int("pageNum") ?: int("page") ?: defaultPageNum,
        pageSize = int("pageSize") ?: defaultPageSize,
        list = jsonArray("list").map { it.asObject().toCollectionItem() }
    )

private fun JsonObject.toCollectionItem(): CollectionItem = CollectionItem(
    postId = long("postId") ?: long("id") ?: 0L,
    authorId = long("authorId") ?: long("userId") ?: 0L,
    authorAvatar = stringValue("authorAvatar") ?: stringValue("avatar"),
    nickname = stringValue("nickname") ?: stringValue("authorName") ?: stringValue("userName") ?: "",
    module = stringValue("module") ?: stringValue("board"),
    title = stringValue("title"),
    summary = stringValue("summary") ?: stringValue("content") ?: "",
    publishTime = stringValue("publishTime"),
    likeCount = int("likeCount") ?: 0,
    collectCount = int("collectCount") ?: 0,
    replyCount = int("replyCount") ?: int("commentCount") ?: 0,
    isLiked = boolean("isLiked") ?: false,
    mediaList = stringList("mediaList")
)
