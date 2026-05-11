package com.vortexa.model

import com.google.gson.annotations.SerializedName

/**
 * 搜索提示接口响应体（/v/api/home/search/suggest）。
 *
 * @param suggestions 热搜/搜索提示列表
 */
data class SearchSuggestResponse(
    val suggestions: List<String>
)

/**
 * 搜索结果接口请求体（/v/api/search/result POST）。
 * 分页参数 pageNum、pageSize 通过 URL Query 传递，不放在 Body 中。
 */
data class SearchResultRequest(
    val keyword: String = "",
    val type: String = "general"
)

/**
 * 搜索结果接口响应体（/v/api/search/result）。
 * list 为混合类型（Post/User/Teacher/Course），通过 type 字段区分。
 */
data class SearchResultResponse(
    val pageNum: Int,
    val pageSize: Int,
    val total: Int,
    val list: List<SearchResultListItem>
)

/**
 * 搜索结果单条，兼容 Post/User/Teacher/Course。仅适配 Post 时解析 type=="Post" 的字段。
 */
data class SearchResultListItem(
    val type: String = "",
    val postId: Long? = null,
    val userId: Long? = null,
    val nickname: String? = null,
    val avatar: String? = null,
    val title: String? = null,
    val summary: String? = null,
    val mediaList: List<String>? = null,
    val totalMediaCount: Int? = null,
    @SerializedName(value = "module", alternate = ["board"])
    val module: String? = null,
    val likeCount: Int? = null,
    val collectCount: Int? = null,
    val replyCount: Int? = null,
    val isLiked: Boolean = false,
    val isCollect: Boolean = false,
    val isInteractionHot: Boolean = false,
    val isViewHot: Boolean = false,
    val publishTime: String? = null
)
