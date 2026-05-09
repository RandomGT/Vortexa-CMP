package com.vortexa.model

import com.google.gson.annotations.SerializedName

/**
 * 预约详情接口 /v/api/c2c/teacher/reserve/detail 的 data 结构。
 * 学员端/导师端共用。
 *
 * @param reserveId 预约订单 ID
 * @param status 状态枚举串（如 TO_START）或中文，展示前建议在 UI 层映射中文
 * @param userId 学员 ID
 * @param studentName 学员昵称/姓名（课堂小助手导师端展示）
 * @param reserveCreateTime 预约创建时间
 * @param courseStartTime 课程开始时间
 * @param hour 时长（小时）
 * @param teacherId 导师 ID
 * @param teacherName 导师姓名
 * @param teacherAvatar 导师头像 URL
 * @param orderPrice 订单价格（暂缓）
 * @param payAmount 实付金额（暂缓）
 * @param payType 支付方式，如「积分」（暂缓）
 * @param cancelUserId 取消操作人用户 ID（有则用于展示「学员/导师已取消该预约」）
 * @param cancelRole 取消方：`student` / `teacher` / `tutor` 等
 * @param cancelOperator 取消操作方：`student`（学员取消）/ `teacher`（导师取消）；与 [cancelRole] 同时存在时解析以本字段为准
 */
data class ReserveDetail(
    @SerializedName("reserveId") val reserveId: Long,
    @SerializedName("status") val status: String,
    @SerializedName("userId") val userId: Long,
    @SerializedName("studentName") val studentName: String? = null,
    @SerializedName("reserveCreateTime") val reserveCreateTime: String,
    @SerializedName("courseStartTime") val courseStartTime: String,
    @SerializedName("hour") val hour: Int,
    @SerializedName("teacherId") val teacherId: Long,
    @SerializedName("teacherName") val teacherName: String,
    @SerializedName("teacherAvatar") val teacherAvatar: String? = null,
    @SerializedName("orderPrice") val orderPrice: String? = null,
    @SerializedName("payAmount") val payAmount: String? = null,
    @SerializedName("payType") val payType: String? = null,
    @SerializedName("channelName") val channelName: String? = null,
    @SerializedName(value = "cancelUserId", alternate = ["cancelByUserId"])
    val cancelUserId: Long? = null,
    @SerializedName(value = "cancelRole", alternate = ["cancelByRole", "cancelledByRole", "cancelUserRole"])
    val cancelRole: String? = null,
    @SerializedName("cancelOperator") val cancelOperator: String? = null,
)
