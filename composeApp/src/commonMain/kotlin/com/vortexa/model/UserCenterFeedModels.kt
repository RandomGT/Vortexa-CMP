package com.vortexa.model

/**
 * 他人/个人中心「回复列表」分页（GET /v/api/user/center/info/comments/{userId}）。
 */
data class UserCenterCommentsResponse(
    val list: List<UserCenterCommentItem>,
    val total: Int,
    val pageNum: Int,
    val pageSize: Int
)

/**
 * 用户中心回复/评论列表单项。
 */
data class UserCenterCommentItem(
    val commentId: Long,
    val postId: Long,
    val userId: Long,
    val userAvatar: String?,
    val userName: String,
    val content: String,
    val likeCount: Int,
    val publishTime: String,
    val isAuthor: Boolean,
    val isLiked: Boolean,
    val mediaList: List<String>?
)
