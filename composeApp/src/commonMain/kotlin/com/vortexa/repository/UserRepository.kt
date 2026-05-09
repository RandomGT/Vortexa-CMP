package com.vortexa.repository

import com.vortexa.model.AvatarUploadData
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

class UserRepository {
    suspend fun getUserProfile(userId: Long): Result<UserProfileResponse> = Result.success(
        UserProfileResponse(
            userInfo = UserProfileInfo(
                userId = userId,
                avatar = null,
                nickname = "Vortexa",
                followCount = 0,
                fanCount = 0,
                isVerified = false,
                certifications = emptyList(),
            ),
            isFollowed = false,
        ),
    )

    suspend fun getUserCenterInfo(): Result<UserCenterInfo> = Result.success(
        UserCenterInfo(
            userInfo = UserCenterUserInfo(0, "Vortexa", null),
            stats = UserCenterStats(0, 0, 0, 0),
        ),
    )

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
