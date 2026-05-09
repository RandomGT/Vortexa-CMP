package com.vortexa.model

import com.google.gson.annotations.SerializedName

/**
 * 导师个人主页接口 /v/api/c2c/teacher/detail 响应体。
 */
data class TeacherDetailResponse(
    val baseInfo: TeacherDetailBaseInfo,
    val courses: List<TeacherDetailCourse> = emptyList(),
    val evaluates: List<TeacherDetailEvaluate> = emptyList()
)

/**
 * 导师基础信息。
 *
 * @param price 一对一视频单次价格，单位由业务约定（如 USD）
 */
data class TeacherDetailBaseInfo(
    val teacherId: Long,
    val userId: Long,
    @SerializedName(value = "avatar", alternate = ["userAvatar", "avatarUrl", "headImg", "headPortrait"])
    val avatar: String? = null,
    val nickName: String,
    val score: String,
    val introduction: String? = null,
    val completedConsultations: Int = 0,
    val price: Float = 0f
)

/**
 * 导师课程（暂缓使用）。
 */
data class TeacherDetailCourse(
    val id: Long,
    @SerializedName("courseName") val courseName: String
)

/**
 * 导师评价（暂缓使用）。
 */
data class TeacherDetailEvaluate(
    val id: Long,
    val score: String,
    val title: String,
    val comment: String,
    val createTimestamp: Long
)
