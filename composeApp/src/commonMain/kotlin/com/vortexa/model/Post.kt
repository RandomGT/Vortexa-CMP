package com.vortexa.model

import com.google.gson.annotations.SerializedName

/**
 *  desc : Post Entity
 *  @author LuXin
 *  @createTime 2026/2/4
 */
data class Post(
    val id: String,
    val username: String,
    val avatar: Any? = null, // Url or ResId
    val time: String,
    val content: String,
    val images: List<String> = emptyList(), // Urls or ResIds
    val tagName: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isLiked: Boolean = false,
    /** 当前用户是否已收藏（与接口字段 isCollect 一致） */
    val isCollect: Boolean = false,
    
    // New fields from API
    val userId: Long = 0,
    val title: String? = null,
    val summary: String? = null,
    val totalMediaCount: Int = 0,
    /** 板块/分区名称，与接口字段 module 一致 */
    val module: String? = null,
    val isInteractionHot: Boolean = false,
    val isViewHot: Boolean = false,
    val collectCount: Int = 0,
    val publishTime: String? = null,
    /** 浏览记录等场景：列表项左上角展示的浏览时间 */
    val viewTime: String? = null
)

data class RecommendPostResponse(
    val total: Int,
    val pageNum: Int,
    val pageSize: Int,
    val list: List<PostItem>
)

data class PostItem(
    val postId: Long,
    val userId: Long,
    val nickname: String,
    val avatar: String?,
    val title: String?,
    val summary: String?,
    val mediaList: List<String>?,
    val totalMediaCount: Int,
    @SerializedName(value = "module", alternate = ["board"])
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

/**
 * 推荐课程接口 /v/api/home/course 的响应 data。
 */
data class RecommendCourseResponse(
    val pageNum: Int,
    val pageSize: Int,
    val total: Int,
    val list: List<RecommendCourseItem>
)

/**
 * 推荐课程列表项。
 */
data class RecommendCourseItem(
    val courseId: Long,
    val cover: String? = null,
    val title: String,
    val authorId: Long? = null,
    val avatar: String? = null,
    val authorNickname: String? = null,
    val studentCount: Int = 0
)

data class RecommendTeacherResponse(
    val total: Int,
    val pageNum: Int,
    val pageSize: Int,
    val list: List<TeacherItem>
)

data class TeacherItem(
    val teacherId: Long,
    val avatar: String?,
    val nickname: String,
    val tags: List<String>?,
    val price: Float,
    /** 推荐导师接口多为字符串（如 "4.9"），也可能为空串；与 [TeacherListItem.score] 对齐 */
    val score: String = ""
)

/**
 * 已关注用户（Figma 747-85098 关注页横向列表项）
 */
data class FollowedUser(
    val userId: Long,
    val nickname: String,
    val avatar: String?,
    /** 是否有新动态，显示小红点 */
    val hasNewPost: Boolean = false
)
