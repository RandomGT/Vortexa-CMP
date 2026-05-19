package com.vortexa.ui.page.profile.paper.management

import android.util.Log
import com.vortexa.model.UserPostItem
import com.vortexa.model.UserPostsResponse
import com.vortexa.net.ApiClient
import com.vortexa.net.ApiException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 稿件管理 Repository。
 * 负责 POST /v/api/user/posts 接口调用与数据转换。
 */
class PaperManagementRepository(
    private val client: ApiClient = ApiClient
) {

    /**
     * 获取稿件管理列表。
     *
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @param status 0 全部，1 草稿箱，2 发布成功，3 未过审，4 审核中；null 表示全部
     * @param searchKeyword 关键字搜索
     * @param sortBy 排序方式
     */
    suspend fun getPosts(
        pageNum: Int = 1,
        pageSize: Int = 20,
        status: Int? = null,
        searchKeyword: String? = null,
        sortBy: String = "newest"
    ): Result<UserPostsResponse> = runCatching {
        Log.d(TAG, "getPosts: pageNum=$pageNum, pageSize=$pageSize, status=$status")
        val response = client.postJson(
            path = "v/api/user/posts",
            query = mapOf("pageNum" to pageNum, "pageSize" to pageSize),
            body = buildJsonObject {
                if (status != null) put("status", status)
                if (!searchKeyword.isNullOrBlank()) put("searchKeyword", searchKeyword)
                put("sortBy", sortBy)
            }
        )
        val data = response.data as? JsonObject
            ?: throw ApiException(-1, "Response data is null")
        data.toUserPostsResponse(defaultPageNum = pageNum, defaultPageSize = pageSize)
    }

    /**
     * 将 [UserPostItem] 映射为列表展示数据。
     */
    fun mapToPaperItemData(item: UserPostItem): PaperItemData = PaperItemData(
        postId = item.postId,
        board = item.module?.takeIf { it.isNotBlank() },
        avatarUrl = item.authorAvatar,
        name = item.authorName.orEmpty().ifEmpty { "未知用户" },
        statusText = item.statusText.orEmpty().ifEmpty { "未知" },
        dateText = firstNonBlankText(item.createdTime, item.createdAt, item.publishTime, item.updatedAt),
        title = item.title.orEmpty(),
        description = item.summary.orEmpty(),
        content = item.summary.orEmpty(),
        likeCount = formatCount(item.likeCount),
        commentCount = formatCount(item.replyCount)
    )

    private fun JsonObject.toUserPostsResponse(
        defaultPageNum: Int,
        defaultPageSize: Int
    ): UserPostsResponse = UserPostsResponse(
        total = int("total") ?: jsonArray("list").size,
        pageNum = int("pageNum") ?: defaultPageNum,
        pageSize = int("pageSize") ?: defaultPageSize,
        list = jsonArray("list").map { it.toUserPostItem() }
    )

    private fun JsonObject.toUserPostItem(): UserPostItem = UserPostItem(
        authorId = long("authorId") ?: long("userId") ?: 0L,
        authorAvatar = string("authorAvatar") ?: string("avatar"),
        authorName = string("authorName") ?: string("nickname") ?: string("userName"),
        postId = long("postId") ?: long("id") ?: 0L,
        module = string("module") ?: string("board"),
        title = string("title"),
        summary = string("summary") ?: string("content"),
        status = string("status"),
        statusText = string("statusText") ?: string("status_text"),
        publishTime = string("publishTime") ?: string("publish_time"),
        createdTime = string("createdTime") ?: string("created_time"),
        createdAt = string("createdAt") ?: string("created_at"),
        updatedAt = string("updatedAt") ?: string("updated_at"),
        replyCount = int("replyCount") ?: int("commentCount") ?: 0,
        likeCount = int("likeCount") ?: 0
    )

    private fun JsonObject.jsonArray(key: String): List<JsonObject> =
        (this[key] as? JsonArray)?.mapNotNull { it as? JsonObject }.orEmpty()

    private fun JsonObject.string(key: String): String? =
        (this[key] as? JsonPrimitive)?.contentOrNull

    private fun JsonObject.int(key: String): Int? =
        (this[key] as? JsonPrimitive)?.intOrNull

    private fun JsonObject.long(key: String): Long? =
        (this[key] as? JsonPrimitive)?.longOrNull

    private fun firstNonBlankText(vararg values: String?): String =
        values.firstNotNullOfOrNull { it?.takeIf(String::isNotBlank) }.orEmpty()

    private fun formatCount(count: Int): String = when {
        count >= 1_000_000 -> {
            val n = count / 1_000_000.0
            if (n == n.toInt().toDouble()) "${n.toInt()}M" else "${roundOneDecimal(n)}M"
        }
        count >= 1_000 -> {
            val n = count / 1_000.0
            if (n == n.toInt().toDouble()) "${n.toInt()}K" else "${roundOneDecimal(n)}K"
        }
        else -> count.toString()
    }

    private fun roundOneDecimal(value: Double): String {
        val rounded = (value * 10).toInt() / 10.0
        return rounded.toString()
    }

    companion object {
        private const val TAG = "PaperManagementRepository"
    }
}
