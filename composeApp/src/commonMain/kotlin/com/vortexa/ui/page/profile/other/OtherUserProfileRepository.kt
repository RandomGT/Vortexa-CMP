package com.vortexa.ui.page.profile.other

import com.vortexa.model.DynamicPostListItem
import com.vortexa.model.DynamicPostsResponse
import com.vortexa.model.UserCenterCommentItem
import com.vortexa.model.UserCenterCommentsResponse
import com.vortexa.net.ApiClient
import com.vortexa.net.ApiException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

/**
 * 他人主页的发帖 / 回复分页接口，封装在 profile/other 目录内避免扩大共享仓库边界。
 */
class OtherUserProfileRepository(
    private val client: ApiClient = ApiClient
) {
    suspend fun getUserCenterPosts(
        userId: Long,
        pageNum: Int = 1,
        pageSize: Int = 5
    ): Result<DynamicPostsResponse> = runCatching {
        val response = client.getJson(
            "v/api/user/center/info/posts",
            mapOf(
                "userId" to userId,
                "pageNum" to pageNum,
                "pageSize" to pageSize
            )
        )
        val data = response.dataObject()
        DynamicPostsResponse(
            pageNum = data.int("pageNum") ?: pageNum,
            pageSize = data.int("pageSize") ?: pageSize,
            total = data.int("total") ?: data.array("list").size,
            list = data.array("list").map { it.asObject().toDynamicPostListItem() }
        )
    }

    suspend fun getUserCenterComments(
        userId: Long,
        pageNum: Int = 1,
        pageSize: Int = 10
    ): Result<UserCenterCommentsResponse> = runCatching {
        val response = client.getJson(
            "v/api/user/center/info/comments",
            mapOf(
                "userId" to userId,
                "pageNum" to pageNum,
                "pageSize" to pageSize
            )
        )
        val data = response.dataObject()
        UserCenterCommentsResponse(
            list = data.array("list").map { it.asObject().toUserCenterCommentItem() },
            total = data.int("total") ?: data.array("list").size,
            pageNum = data.int("pageNum") ?: pageNum,
            pageSize = data.int("pageSize") ?: pageSize
        )
    }
}

private fun com.vortexa.net.ApiResponse.dataObject(): JsonObject =
    data as? JsonObject ?: throw ApiException(-1, "Response data is null")

private fun kotlinx.serialization.json.JsonElement.asObject(): JsonObject =
    this as? JsonObject ?: JsonObject(emptyMap())

private fun JsonObject.array(key: String): List<kotlinx.serialization.json.JsonElement> =
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
    (this[key] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.takeIf(JsonPrimitive::isString)?.content }

private fun JsonObject.toDynamicPostListItem(): DynamicPostListItem = DynamicPostListItem(
    postId = long("postId") ?: long("id") ?: 0L,
    userId = long("userId") ?: 0L,
    nickname = string("nickname") ?: string("userName") ?: "",
    avatar = string("avatar") ?: string("userAvatar"),
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
    publishTime = string("publishTime")
)

private fun JsonObject.toUserCenterCommentItem(): UserCenterCommentItem = UserCenterCommentItem(
    commentId = long("commentId") ?: long("id") ?: 0L,
    postId = long("postId") ?: 0L,
    userId = long("userId") ?: 0L,
    userAvatar = string("userAvatar") ?: string("avatar"),
    userName = string("userName") ?: string("nickname") ?: "",
    content = string("content") ?: "",
    likeCount = int("likeCount") ?: 0,
    publishTime = string("publishTime") ?: "",
    isAuthor = boolean("isAuthor") ?: false,
    isLiked = boolean("isLiked") ?: false,
    mediaList = stringList("mediaList")
)
