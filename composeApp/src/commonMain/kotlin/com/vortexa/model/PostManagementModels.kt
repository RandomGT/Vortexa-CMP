package com.vortexa.model

import com.google.gson.annotations.SerializedName

/**
 * 稿件管理列表请求体（POST /v/api/user/posts，与 pageNum/pageSize 的 Query 配合使用）。
 *
 * @param status 0 全部，1 草稿箱，2 发布成功，3 未过审，4 审核中；null 表示全部
 * @param searchKeyword 关键字搜索
 * @param sortBy 排序：newest/oldest/most_clicks/most_replies/most_likes，默认 newest
 */
data class UserPostsRequest(
    val status: Int? = null,
    val searchKeyword: String? = null,
    val sortBy: String = "newest"
)

/**
 * 稿件管理接口响应体（POST /v/api/user/posts）。
 *
 * @param total 总条数
 * @param pageNum 当前页码
 * @param pageSize 每页条数
 * @param list 稿件列表
 */
data class UserPostsResponse(
    val total: Int,
    val pageNum: Int,
    val pageSize: Int,
    val list: List<UserPostItem>
)

/**
 * 稿件管理单条。
 *
 * @param authorId 作者 ID
 * @param authorAvatar 作者头像 URL
 * @param authorName 作者昵称
 * @param postId 帖子 ID
 * @param title 标题
 * @param summary 摘要
 * @param status 状态：draft/reviewing/published/rejected
 * @param statusText 状态文案，如「审核中」「发布成功」
 * @param publishTime 发布时间
 * @param createdTime 创建时间（接口字段，与 createdAt 二选一或并存）
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 * @param replyCount 评论数
 * @param likeCount 点赞数
 * @param module 发布板块（与发帖 module 一致，如 杂谈）
 */
data class UserPostItem(
    val authorId: Long,
    val authorAvatar: String? = null,
    val authorName: String? = null,
    val postId: Long,
    /** 发布板块，编辑时需回传更新接口 */
    val module: String? = null,
    val title: String? = null,
    val summary: String? = null,
    val status: String? = null,
    val statusText: String? = null,
    val publishTime: String? = null,
    @SerializedName(value = "createdTime", alternate = ["created_time"])
    val createdTime: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val replyCount: Int = 0,
    val likeCount: Int = 0
)
