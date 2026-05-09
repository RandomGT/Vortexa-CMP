package com.vortexa.model

/**
 * 创作中心近 x 日数据（GET /v/api/user/creator/data/{days}）
 *
 * @param userId 用户 ID
 * @param days 统计天数
 * @param postCount 发帖数
 * @param viewCount 内容浏览
 * @param likeCount 点赞数
 * @param commentCount 评论数
 * @param followerGrowth 粉丝增长
 * @param pageVisitors 页面访客
 * @param shares 分享数
 * @param revenue 收益
 */
data class CreatorData(
    val userId: Long,
    val days: Int,
    val postCount: Int,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val followerGrowth: Int,
    val pageVisitors: Int,
    val shares: Int,
    val revenue: Int
)

/**
 * GET /v/api/user/profile/{userId} 响应中 [ApiResponse.data] 的结构。
 */
data class CreatorProfileData(
    val userInfo: CreatorProfileUserInfo?,
    val certifications: List<CreatorCertification>? = null,
    val isFollowed: Boolean? = null
)

/**
 * 个人页 / 创作中心 profile 接口中的 [CreatorProfileData.userInfo]。
 */
data class CreatorProfileUserInfo(
    val avatar: String?,
    val userId: Long,
    val nickname: String?,
    val followCount: Int? = null,
    val fanCount: Int? = null,
    val intro: String? = null,
    val isVerified: Boolean? = null
)

/**
 * 创作中心展示用的用户信息（由 [CreatorProfileData] 映射而来）
 *
 * @param userId 用户 ID
 * @param userAvatar 头像 URL（对应接口 avatar）
 * @param userName 用户昵称（对应接口 nickname）
 * @param certifications 认证列表（根级 certifications）
 * @param isFollowed 当前登录用户是否已关注该主页用户
 */
data class CreatorUserInfo(
    val userId: Long,
    val userAvatar: String?,
    val userName: String?,
    val certifications: List<CreatorCertification>?,
    val isFollowed: Boolean = false,
    val followCount: Int = 0,
    val fanCount: Int = 0,
    val intro: String? = null,
    val isVerified: Boolean = false
)

/**
 * 创作者认证项
 */
data class CreatorCertification(
    val type: String,
    val name: String
)

/**
 * 创作中心活动项（活动 Banner）
 *
 * @param id 活动 ID
 * @param title 活动标题
 * @param cover 封面图 URL
 * @param startAt 开始时间
 * @param endAt 结束时间
 * @param status 状态：ongoing/upcoming 等
 * @param detailUrl 详情页 URL
 */
data class CreatorActivity(
    val id: Long,
    val title: String,
    val cover: String?,
    val startAt: String?,
    val endAt: String?,
    val status: String?,
    val detailUrl: String?
)

/**
 * 创作中心活动列表响应（GET /v/api/user/creator/activities）
 *
 * @param page 当前页码
 * @param pageSize 每页条数
 * @param total 总条数
 * @param list 活动列表
 */
data class CreatorActivityListResponse(
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val list: List<CreatorActivity>
)

/**
 * 创作者激励任务项（GET /v/api/user/creator/tasks）
 *
 * @param id 任务 ID
 * @param title 任务标题
 * @param description 任务描述
 * @param status 状态：completed/unfinished/claimed
 * @param progress 当前进度
 * @param target 目标值
 * @param reward 奖励类型（如创作积分）
 * @param rewardAmount 奖励数量
 * @param canClaim 是否可领取
 * @param completedAt 完成时间
 * @param claimedAt 领取时间
 */
data class CreatorTask(
    val id: Long,
    val title: String,
    val description: String?,
    val status: String?,
    val progress: Int,
    val target: Int,
    val reward: String?,
    val rewardAmount: Int?,
    val canClaim: Boolean,
    val completedAt: String?,
    val claimedAt: String?
)

/**
 * 贴文数据一览单项（GET /v/api/user/posts/data/{days} 响应项）
 */
data class PostDataItem(
    val authorId: Long,
    val authorAvatar: String?,
    val authorName: String?,
    val postId: String,
    val title: String?,
    val summary: String?,
    val mediaList: List<String>?,
    val totalMediaCount: Int,
    val viewCount: Long,
    val likeCount: Int,
    val collectCount: Int,
    val replyCount: Int,
    val publishTime: String?
)

/**
 * 贴文数据一览列表响应（GET /v/api/user/posts/data/{days}）
 */
data class PostDataListResponse(
    val pageNum: Int,
    val pageSize: Int,
    val total: Int,
    val postList: List<PostDataItem>
)

/**
 * 数据中心帖文列表一页（含接口 [PostDataListResponse.total] 用于分页）
 */
data class CreatorPostDataPage(
    val list: List<CreatorStatisticsPostItem>,
    val total: Int
)

/**
 * 数据中心帖子统计项（帖子列表缩略视图）
 *
 * @param postId 帖子 ID
 * @param nickname 作者昵称
 * @param avatar 头像 URL
 * @param publishTime 发布时间
 * @param title 标题
 * @param summary 摘要（一行展示）
 * @param viewCount 浏览量
 * @param likeCount 点赞数
 * @param replyCount 评论数
 * @param shareCount 分享数
 * @param revenue 收益
 */
data class CreatorStatisticsPostItem(
    val postId: Long,
    val nickname: String,
    val avatar: String?,
    val publishTime: String?,
    val title: String?,
    val summary: String?,
    val viewCount: Long,
    val likeCount: Int,
    val replyCount: Int,
    val shareCount: Int,
    val revenue: Int
)

/**
 * 创作者帖子统计列表响应（GET /v/api/user/creator/statistics/posts）
 */
data class CreatorStatisticsPostListResponse(
    val total: Int,
    val pageNum: Int,
    val pageSize: Int,
    val list: List<CreatorStatisticsPostItem>
)

/**
 * 创作者激励任务列表响应（GET /v/api/user/creator/tasks）
 */
data class CreatorTaskListResponse(
    val pageNum: Int,
    val pageSize: Int,
    val total: Int,
    val list: List<CreatorTask>
)
