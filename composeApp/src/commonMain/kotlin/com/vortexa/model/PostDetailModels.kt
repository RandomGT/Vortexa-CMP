package com.vortexa.model

/**
 * 贴文详情接口响应体（/v/api/home/posts/{postId}）。
 *
 * @param authorInfo 作者信息
 * @param postInfo 贴文信息
 */
data class PostDetailResponse(
    val authorInfo: AuthorInfo,
    val postInfo: PostInfo
)

/**
 * 贴文详情-作者信息
 *
 * @param authorId 作者 ID
 * @param authorAvatar 头像 URL
 * @param authorName 昵称
 * @param isFollowed 当前用户是否已关注
 */
data class AuthorInfo(
    val authorId: Long,
    val authorAvatar: String?,
    val authorName: String,
    val isFollowed: Boolean
)

/**
 * 贴文详情-贴文信息
 *
 * @param postId 贴文 ID
 * @param title 标题
 * @param content 正文
 * @param contentFormat 正文格式，如 "HTML" 时 [content] 为 HTML 片段，按 HTML 解析展示
 * @param module 分区/板块（接口可能返回 module，与发帖时 body.module 对应）
 * @param board 分区展示名（部分接口用 board；详情展示优先 module）
 * @param likeCount 点赞数
 * @param collectCount 收藏数
 * @param replyCount 回复数
 * @param isLiked 当前用户是否已点赞
 * @param isCollect 当前用户是否已收藏
 * @param publishTime 发布时间
 * @param mediaList 媒体资源列表（图片/视频 URL）
 * @param totalMediaCount 媒体总数
 */
data class PostInfo(
    val postId: Long,
    val title: String?,
    val content: String?,
    val contentFormat: String? = null,
    val module: String? = null,
    val board: String?,
    val likeCount: Int,
    val collectCount: Int,
    val replyCount: Int,
    val isLiked: Boolean = false,
    val isCollect: Boolean,
    val publishTime: String?,
    val mediaList: List<String>? = null,
    val totalMediaCount: Int? = null
)

/**
 * 帖子评论接口单条（/v/api/home/posts/{postId}/comment）。
 * 仅返回 parentCommentId 为 null 的一级评论。
 *
 * @param commentId 评论 ID
 * @param postId 贴文 ID
 * @param parentCommentId 父评论 ID，一级评论为 null
 * @param userId 评论者 ID
 * @param userAvatar 头像 URL
 * @param userName 昵称
 * @param content 评论内容
 * @param likeCount 点赞数
 * @param publishTime 发布时间
 * @param isAuthor 是否楼主（帖子作者）
 * @param mediaList 媒体列表（图片/视频 URL）
 */
data class PostCommentItem(
    val commentId: Long,
    val postId: Long,
    val parentCommentId: Long?,
    val userId: Long,
    val userAvatar: String?,
    val userName: String,
    val content: String,
    val likeCount: Int,
    val publishTime: String,
    val isAuthor: Boolean,
    val isLiked: Boolean,
    val mediaList: List<String>? = null
)

/**
 * 评论回复接口单条（/v/api/home/comments/{commentId}/replies）。
 * parentCommentId 等于 commentId，replyToUserId 标识回复对象（null 表示回复评论作者）。
 *
 * @param commentId 回复 ID
 * @param postId 贴文 ID
 * @param parentCommentId 父评论 ID
 * @param replyToUserId 被回复用户 ID，null 表示回复评论作者
 * @param userId 回复者 ID
 * @param userAvatar 头像 URL
 * @param userName 昵称
 * @param content 回复内容
 * @param likeCount 点赞数
 * @param isAuthor 是否楼主
 * @param publishTime 发布时间
 * @param mediaList 媒体列表（图片/视频 URL）
 */
data class CommentReplyItem(
    val commentId: Long,
    val postId: Long,
    val parentCommentId: Long,
    val replyToUserId: Long?,
    val userId: Long,
    val userAvatar: String?,
    val userName: String,
    val content: String,
    val likeCount: Int,
    val isAuthor: Boolean,
    val publishTime: String,
    val isLiked: Boolean = false,
    val mediaList: List<String>? = null
)

/**
 * 发布评论/回复请求体（POST /v/api/home/discussion/comments）。
 *
 * @param postId 评论所属贴文 ID，必填
 * @param parentCommentId 父评论 ID：为空表示对贴文发表评论，有值表示对该评论进行回复
 * @param content 结构化富文本内容，支持文本、话题、@用户、表情、图片、视频、链接，必填
 * @param mediaList 媒体列表（图片/视频 URL），来自输入框选中的图片、视频上传后的地址
 */
data class PostCommentRequest(
    val postId: Long,
    val parentCommentId: Long? = null,
    val content: String,
    val mediaList: List<String>? = null
)
