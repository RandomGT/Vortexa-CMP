package com.vortexa.ui.page.post.detail.reply

/**
 * 评论数据模型
 * 后续由接口替换
 */
data class Comment(
    val id: String,
    val authorName: String,
    val avatar: Any? = null,
    /** 评论者用户 ID，与发帖人一致且展示「楼主」时用于 inline 关注按钮 */
    val userId: Long = 0L,
    val isAuthor: Boolean, // 是否楼主（帖子作者）
    val content: String,
    val images: List<Any> = emptyList(),
    val likeCount: Int = 0,
    val isLiked: Boolean = false, // 当前用户是否已点赞
    val time: String,
    val replies: List<Reply> = emptyList(),
    /** 是否还有未拉取的回复（与 nextReplyPage 配合用于分页） */
    val hasMoreReplies: Boolean = false,
    /** 下一页回复的页码（首屏加载完若 hasMoreReplies 则为 2） */
    val nextReplyPage: Int = 1,
    /** 正在加载该评论的更多回复 */
    val repliesLoadingMore: Boolean = false
)

/**
 * 回复数据模型
 */
data class Reply(
    val id: String,
    val authorName: String,
    val avatar: Any? = null,
    val userId: Long = 0L,
    /** 是否楼主（帖子作者） */
    val isAuthor: Boolean = false,
    val replyToName: String, // 回复谁
    val content: String,
    val images: List<Any> = emptyList(), // 媒体列表（图片/视频 URL）
    val likeCount: Int = 0,
    val isLiked: Boolean = false, // 当前用户是否已点赞
    val time: String
)

/**
 * 回复目标信息，用于在底部输入栏上方展示"回复 xxx"指示条
 * @param id 目标评论/回复 ID（一级评论 id 或二级回复 id）
 * @param authorName 被回复人昵称
 * @param avatar 被回复人头像
 * @param content 被回复的原文
 * @param isComment true=回复一级评论，false=回复二级回复
 * @param rootCommentId 当 isComment=false 时必填，表示所在楼层的一级评论 ID，用于接口 parentCommentId
 */
data class ReplyTarget(
    val id: String,
    val authorName: String,
    val avatar: Any? = null,
    val content: String,
    val isComment: Boolean,
    val rootCommentId: String? = null
)
