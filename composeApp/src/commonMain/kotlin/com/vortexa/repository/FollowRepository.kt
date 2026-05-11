package com.vortexa.repository

import com.vortexa.model.DynamicPostListItem
import com.vortexa.model.DynamicPostsResponse
import com.vortexa.model.FollowedUser
import com.vortexa.model.FollowingListItem
import com.vortexa.model.FollowingListResponse
import com.vortexa.model.Post
import com.vortexa.model.toPost
import com.vortexa.net.ApiClient
import com.vortexa.net.ApiException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

class FollowRepository(
    private val client: ApiClient = ApiClient
) {
    suspend fun getFollowingList(pageNum: Int = 1, pageSize: Int = 20, type: Int = 0): Result<FollowingListResponse> =
        runCatching {
            val response = client.getJson(
                "v/api/dynamic/followingList",
                mapOf("pageNum" to pageNum, "pageSize" to pageSize, "Type" to type)
            )
            val data = response.dataObject()
            FollowingListResponse(
                pageNum = data.int("pageNum") ?: pageNum,
                pageSize = data.int("pageSize") ?: pageSize,
                total = data.int("total") ?: data.array("list").size,
                list = data.array("list").map { it.asObject().toFollowingListItem() }
            )
        }

    suspend fun getDynamicPosts(
        pageNum: Int = 1,
        pageSize: Int = 20,
        followingId: Long? = null
    ): Result<DynamicPostsResponse> =
        runCatching {
            val response = client.getJson(
                "v/api/dynamic/posts",
                mapOf("pageNum" to pageNum, "pageSize" to pageSize, "followingId" to followingId)
            )
            val data = response.dataObject()
            DynamicPostsResponse(
                pageNum = data.int("pageNum") ?: pageNum,
                pageSize = data.int("pageSize") ?: pageSize,
                total = data.int("total") ?: data.array("list").size,
                list = data.array("list").map { it.asObject().toDynamicPostListItem() }
            )
        }

    fun mapDynamicItemToPost(item: DynamicPostListItem): Post = item.toPost()

    fun mapToFollowedUser(item: FollowingListItem): FollowedUser = FollowedUser(
        userId = item.userId,
        nickname = item.userName.ifEmpty { "用户" },
        avatar = item.userAvatar?.takeIf { it.isNotBlank() },
        hasNewPost = item.recentInteraction > 0,
    )
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
    (this[key] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.content }

private fun JsonObject.toFollowingListItem(): FollowingListItem = FollowingListItem(
    userId = long("userId") ?: 0L,
    userName = string("userName") ?: string("nickname") ?: "",
    userAvatar = string("userAvatar") ?: string("avatar"),
    fanCount = int("fanCount") ?: 0,
    postCount = int("postCount") ?: 0,
    recentInteraction = int("recentInteraction") ?: 0
)

private fun JsonObject.toDynamicPostListItem(): DynamicPostListItem = DynamicPostListItem(
    postId = long("postId") ?: long("id") ?: 0L,
    userId = long("userId") ?: 0L,
    nickname = string("nickname") ?: string("userName") ?: "",
    avatar = string("avatar"),
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
