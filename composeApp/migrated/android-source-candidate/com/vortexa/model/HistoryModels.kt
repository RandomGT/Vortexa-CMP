package com.vortexa.model

import com.google.gson.annotations.SerializedName

/**
 * 浏览记录接口响应体（GET /v/api/user/viewHistory）。
 *
 * @param total 总条数
 * @param page 当前页码
 * @param pageSize 每页条数
 * @param list 浏览记录列表
 */
data class ViewHistoryResponse(
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val list: List<ViewHistoryItem>
)

/**
 * 浏览记录单条（与帖子列表项字段一致，含 type=post）。
 *
 * @param type 记录类型，如 post
 * @param postId 帖子 ID
 * @param userId 作者用户 ID
 * @param nickname 作者昵称
 * @param avatar 作者头像 URL
 * @param title 标题
 * @param summary 摘要/正文节选
 * @param mediaList 媒体 URL 列表
 * @param totalMediaCount 媒体总数
 * @param module 板块名称
 * @param publishTime 发布时间展示文案
 * @param viewTime 浏览时间展示文案（列表项左上角）
 */
data class ViewHistoryItem(
    val type: String? = null,
    val postId: Long,
    val userId: Long,
    val nickname: String,
    val avatar: String?,
    val title: String?,
    val summary: String?,
    val mediaList: List<String>?,
    val totalMediaCount: Int = 0,
    @SerializedName(value = "module", alternate = ["board"])
    val module: String?,
    val isInteractionHot: Boolean = false,
    val isViewHot: Boolean = false,
    val likeCount: Int = 0,
    val collectCount: Int = 0,
    val replyCount: Int = 0,
    val isLiked: Boolean = false,
    val isCollect: Boolean = false,
    val publishTime: String? = null,
    val viewTime: String? = null
)
