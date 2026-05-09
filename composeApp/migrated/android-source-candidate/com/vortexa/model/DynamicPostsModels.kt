package com.vortexa.model

/**
 * 获取动态列表接口响应体（GET /v/api/dynamic/posts）。
 * 不传 followingId 时返回所有关注者动态按时间排序。
 *
 * @param pageNum 当前页码
 * @param pageSize 每页条数
 * @param total 总条数
 * @param list 动态帖子列表
 */
data class DynamicPostsResponse(
    val pageNum: Int,
    val pageSize: Int,
    val total: Int,
    val list: List<DynamicPostListItem>
)

/**
 * 动态列表单条帖子。
 *
 * @param postId 帖子 ID
 * @param userId 用户 ID
 * @param nickname 昵称
 * @param avatar 头像 URL
 * @param title 标题
 * @param summary 摘要/内容摘要
 * @param mediaList 媒体 URL 列表
 * @param totalMediaCount 媒体总数
 * @param module 分区（如「杂谈」），接口字段名为 module
 * @param isInteractionHot 是否互动热门
 * @param isViewHot 是否浏览热门
 * @param likeCount 点赞数
 * @param collectCount 收藏数
 * @param replyCount 评论数
 * @param isLiked 当前用户是否已点赞
 * @param isCollect 当前用户是否已收藏
 * @param publishTime 发布时间
 */
data class DynamicPostListItem(
    val postId: Long,
    val userId: Long,
    val nickname: String,
    val avatar: String?,
    val title: String?,
    val summary: String?,
    val mediaList: List<String>?,
    val totalMediaCount: Int,
    val module: String?,
    val isInteractionHot: Boolean = false,
    val isViewHot: Boolean = false,
    val likeCount: Int,
    val collectCount: Int,
    val replyCount: Int,
    val isLiked: Boolean = false,
    val isCollect: Boolean = false,
    val publishTime: String?
)

/** 将动态/个人中心帖子项转为列表用 [Post]（与 [com.vortexa.repository.FollowRepository.mapDynamicItemToPost] 一致）。 */
fun DynamicPostListItem.toPost(): Post = Post(
    id = postId.toString(),
    username = nickname,
    avatar = avatar,
    time = publishTime ?: "",
    content = summary ?: "",
    images = mediaList ?: emptyList(),
    tagName = module,
    likeCount = likeCount,
    commentCount = replyCount,
    isLiked = isLiked,
    isCollect = isCollect,
    userId = userId,
    title = title,
    summary = summary,
    totalMediaCount = totalMediaCount,
    module = module,
    isInteractionHot = isInteractionHot,
    isViewHot = isViewHot,
    collectCount = collectCount,
    publishTime = publishTime
)
