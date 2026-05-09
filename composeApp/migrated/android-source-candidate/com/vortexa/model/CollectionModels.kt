package com.vortexa.model

/**
 * 我的收藏接口请求体（POST /v/api/user/collections，module 放 body）。
 *
 * @param module 板块中文名，如「杂谈」「交易经验」「玩法」；null 或不传表示全部
 */
data class CollectionRequest(
    val module: String? = null
)

/**
 * 我的收藏接口响应体（POST /v/api/user/collections）。
 *
 * @param total 总条数
 * @param pageNum 当前页码
 * @param pageSize 每页条数
 * @param list 收藏帖子列表
 */
data class CollectionResponse(
    val total: Int,
    val pageNum: Int,
    val pageSize: Int,
    val list: List<CollectionItem>
)

/**
 * 收藏列表单条。
 *
 * @param postId 帖子 ID
 * @param authorId 作者 ID
 * @param authorAvatar 作者头像 URL
 * @param authorName 作者昵称
 * @param module 板块：0 全部，1 杂谈，2 交易经验，3 玩法
 * @param title 贴文标题
 * @param summary 内容摘要
 * @param publishTime 发布时间展示文案
 * @param likeCount 点赞数
 * @param collectCount 收藏数
 * @param replyCount 评论数
 * @param isLiked 当前用户是否已点赞该帖
 */
data class CollectionItem(
    val postId: Long,
    val authorId: Long,
    val authorAvatar: String? = null,
    val nickname: String = "",
    val module: String? = "",
    val title: String? = null,
    val summary: String = "",
    val publishTime: String? = null,
    val likeCount: Int = 0,
    val collectCount: Int = 0,
    val replyCount: Int = 0,
    val isLiked: Boolean = false,
    val mediaList: List<String>? = emptyList()
)
