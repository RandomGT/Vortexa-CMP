package com.vortexa.model

import com.google.gson.annotations.SerializedName

/**
 * 导师列表接口 /v/api/c2c/teacher/list 响应体。
 */
data class TeacherListResponse(
    val pageNum: Int,
    val pageSize: Int,
    val total: Int,
    val list: List<TeacherListItem>
)

/**
 * 单条导师信息，与接口字段一致（price/score 接口为 String）。
 */
data class TeacherListItem(
    val teacherId: Long,
    val avatar: String? = null,
    val teacherAvatar: String? = null,
    @SerializedName("nickName") val nickName: String,
    val price: String = "0",
    val score: String = "0",
    @SerializedName("tagList") val tagList: List<String>? = null,
    /** 报价单位文案（如「积分」），与接口 priceUnit 对齐；未返回时由客户端默认「积分」 */
    @SerializedName("priceUnit") val priceUnit: String? = null,
)
