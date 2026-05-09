package com.vortexa.repository

import com.vortexa.model.AvatarUploadData
import com.vortexa.model.Certification
import com.vortexa.model.DeletePostData
import com.vortexa.model.FollowResult
import com.vortexa.model.LikeCommentData
import com.vortexa.model.LikePostData
import com.vortexa.model.UserCenterInfo
import com.vortexa.model.UserCenterStats
import com.vortexa.model.UserCenterUpdateData
import com.vortexa.model.UserCenterUserInfo
import com.vortexa.model.UserProfileInfo
import com.vortexa.model.UserProfileResponse
import com.vortexa.model.WalletPointData
import com.vortexa.net.ApiClient
import com.vortexa.net.ApiException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

class UserRepository(
    private val client: ApiClient = ApiClient
) {
    suspend fun getUserProfile(userId: Long): Result<UserProfileResponse> = runCatching {
        val response = client.getJson("v/api/user/profile/$userId")
        val data = response.data as? JsonObject ?: throw ApiException(-1, "Response data is null")
        UserProfileResponse(
            userInfo = data.obj("userInfo").toUserProfileInfo(userId),
            isFollowed = data.boolean("isFollowed") ?: false
        )
    }

    suspend fun getUserCenterInfo(): Result<UserCenterInfo> = runCatching {
        val response = client.getJson("v/api/user/center/info")
        val data = response.data as? JsonObject ?: throw ApiException(-1, "Response data is null")
        UserCenterInfo(
            userInfo = data.obj("userInfo").toUserCenterUserInfo(),
            stats = data.obj("stats").toUserCenterStats()
        )
    }

    suspend fun follow(userId: Long): Result<FollowResult> = Result.success(FollowResult(userId))
    suspend fun unfollow(userId: Long): Result<FollowResult> = Result.success(FollowResult(userId))
    suspend fun likePost(postId: Long): Result<LikePostData> = Result.success(LikePostData(postId))
    suspend fun unlikePost(postId: Long): Result<LikePostData> = Result.success(LikePostData(postId))
    suspend fun collectPost(postId: Long): Result<LikePostData> = Result.success(LikePostData(postId))
    suspend fun uncollectPost(postId: Long): Result<LikePostData> = Result.success(LikePostData(postId))
    suspend fun likeComment(commentId: Long): Result<LikeCommentData> = Result.success(LikeCommentData(commentId))
    suspend fun unlikeComment(commentId: Long): Result<LikeCommentData> = Result.success(LikeCommentData(commentId))
    suspend fun uploadAvatar(uri: Any): Result<String> = Result.success("")
    suspend fun updateUserCenter(avatar: String? = null, userName: String? = null): Result<UserCenterUpdateData> =
        Result.success(UserCenterUpdateData(0, userName ?: "Vortexa", avatar))
    suspend fun updateUserCenter(userId: Long, avatar: String? = null, userName: String? = null): Result<UserCenterUpdateData> =
        Result.success(UserCenterUpdateData(userId, userName ?: "Vortexa", avatar))
    suspend fun updatePost(
        postId: Long,
        module: String,
        title: String,
        content: String,
        mediaListJson: String? = null,
    ): Result<Unit> = Result.success(Unit)
    suspend fun getWalletPoint(): Result<WalletPointData> = Result.success(WalletPointData(0))
    suspend fun deletePost(postId: Long): Result<DeletePostData> = Result.success(DeletePostData("删除成功"))
}

private fun JsonObject.obj(key: String): JsonObject =
    this[key] as? JsonObject ?: JsonObject(emptyMap())

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

private fun kotlinx.serialization.json.JsonElement.asObject(): JsonObject =
    this as? JsonObject ?: JsonObject(emptyMap())

private fun JsonObject.toUserProfileInfo(defaultUserId: Long): UserProfileInfo = UserProfileInfo(
    userId = long("userId") ?: long("id") ?: defaultUserId,
    avatar = string("avatar"),
    nickname = string("nickname") ?: string("userName"),
    followCount = int("followCount") ?: 0,
    fanCount = int("fanCount") ?: 0,
    isVerified = boolean("isVerified") ?: false,
    certifications = array("certifications").map {
        it.asObject().let { obj ->
            Certification(
                type = obj.string("type") ?: "",
                name = obj.string("name") ?: ""
            )
        }
    },
    teacherId = long("teacherId")
)

private fun JsonObject.toUserCenterUserInfo(): UserCenterUserInfo = UserCenterUserInfo(
    userId = long("userId") ?: long("id") ?: 0L,
    userName = string("userName") ?: string("nickname") ?: "",
    userAvatar = string("userAvatar") ?: string("avatar"),
    teacherId = long("teacherId")
)

private fun JsonObject.toUserCenterStats(): UserCenterStats = UserCenterStats(
    postCount = int("postCount") ?: 0,
    likeCount = int("likeCount") ?: 0,
    followCount = int("followCount") ?: 0,
    fanCount = int("fanCount") ?: 0
)
