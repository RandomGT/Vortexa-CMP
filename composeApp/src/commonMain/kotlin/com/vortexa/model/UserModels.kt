package com.vortexa.model

import com.google.gson.annotations.SerializedName

/**
 * 用户个人主页信息响应体
 */
data class UserProfileResponse(
    val userInfo: UserProfileInfo,
    val isFollowed: Boolean
)

/**
 * 用户详细信息
 */
data class UserProfileInfo(
    val userId: Long,
    val avatar: String?,
    val nickname: String?,
    val followCount: Int,
    val fanCount: Int,
    val isVerified: Boolean,
    val certifications: List<Certification>?,
    /** 该用户的导师业务 ID，非导师或未下发时为 null */
    @SerializedName("teacherId") val teacherId: Long? = null
)

/**
 * 认证信息
 */
data class Certification(
    val type: String,
    val name: String
)

/**
 * 个人中心信息响应体
 * @see com.vortexa.api.UserApi.getUserCenterInfo
 */
data class UserCenterInfo(
    val userInfo: UserCenterUserInfo,
    val stats: UserCenterStats,

)

/**
 * 个人中心用户信息
 */
data class UserCenterUserInfo(
    val userId: Long,
    val userName: String,
    val userAvatar: String?,
    /** 教师端身份时的教师 ID，非教师或未下发时为 null */
    val teacherId: Long? = null
)

/**
 * 个人中心统计数据
 */
data class UserCenterStats(
    val postCount: Int,
    val likeCount: Int,
    val followCount: Int,
    val fanCount: Int
)

/**
 * 编辑头像/昵称请求体（/v/api/user/center/update）
 */
data class UserCenterUpdateRequest(
    val avatar: String? = null,
    val userName: String? = null
)

/**
 * 编辑头像/昵称响应 data 字段
 */
data class UserCenterUpdateData(
    val userId: Long,
    val userName: String,
    val userAvatar: String?
)

/**
 * 头像上传接口响应（返回 CDN URL）
 */
data class AvatarUploadData(
    val url: String
)

/**
 * 积分余额响应体（/v/api/user/wallet/point）
 *
 * @param availablePoints 可用积分余额
 */
data class WalletPointData(
    val availablePoints: Int
)

/**
 * 关注/取消关注接口响应 data 字段
 *
 * @param userId 被操作的用户 ID
 */
data class FollowResult(
    val userId: Long
)

/**
 * 点赞帖子接口响应 data 字段（/v/api/user/like/post/{postId}）
 *
 * @param postId 帖子 ID
 */
data class LikePostData(
    val postId: Long
)

/**
 * 删除帖子接口响应 data（DELETE /v/api/user/posts/{postId}，逻辑删除，仅作者）
 *
 * @param msg 如「删除成功」
 */
data class DeletePostData(
    val msg: String? = null
)

/**
 * 点赞评论接口响应 data 字段（/v/api/user/like/comment/{commentId}）
 *
 * @param commentId 评论 ID
 */
data class LikeCommentData(
    val commentId: Long
)
