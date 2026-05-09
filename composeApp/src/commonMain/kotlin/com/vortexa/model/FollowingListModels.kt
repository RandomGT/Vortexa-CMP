package com.vortexa.model

/**
 * 关注列表接口响应体（GET /v/api/dynamic/followingList）。
 *
 * @param pageNum 当前页码
 * @param pageSize 每页条数
 * @param total 总条数
 * @param list 关注用户列表（按关注顺序排序）
 */
data class FollowingListResponse(
    val pageNum: Int,
    val pageSize: Int,
    val total: Int,
    val list: List<FollowingListItem>
)

/**
 * 关注列表单条。
 *
 * @param userId 用户 ID
 * @param userName 用户昵称
 * @param userAvatar 头像 URL
 * @param fanCount 粉丝数
 * @param postCount 帖子数
 * @param recentInteraction 最近互动（可用于排序或小红点等）
 */
data class FollowingListItem(
    val userId: Long,
    val userName: String = "",
    val userAvatar: String? = null,
    val fanCount: Int = 0,
    val postCount: Int = 0,
    val recentInteraction: Int = 0
)
