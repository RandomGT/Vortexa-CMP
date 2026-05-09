package com.vortexa.model

import com.google.gson.annotations.SerializedName

/**
 * 预约列表单条数据，对应接口 /v/api/c2c/teacher/reserve/list 的 data 数组元素。
 * 学员端/导师端共用，客户端按 [status] 分组展示（全部/进行中/已完成）。
 *
 * @param reserveId 预约订单 ID
 * @param status 状态枚举串，取值见 [ReserveListApiStatus]（展示前由客户端映射中文）
 * @param reserveCreateTime 预约创建时间，如 "2026/03/01 18:00"
 * @param courseStartTime 课程开始时间，如 "2026/03/02 18:00"
 * @param hour 时长（小时）
 * @param userId 用户 ID
 * @param teacherId 导师 ID
 * @param teacherName 导师姓名
 * @param studentName 学员姓名（导师端列表「指导对象」展示；学员端可为空）
 * @param channelName 频道名，用于进入教室等
 * @param orderPrice 订单价格（暂缓展示）
 * @param payAmount 实付金额（暂缓展示）
 * @param payType 支付方式，如 "积分"（暂缓展示）
 */
data class ReserveListItem(
    @SerializedName("reserveId") val reserveId: Long,
    @SerializedName("status") val status: String,
    @SerializedName("reserveCreateTime") val reserveCreateTime: String,
    @SerializedName("courseStartTime") val courseStartTime: String,
    @SerializedName("hour") val hour: Int,
    @SerializedName("userId") val userId: Long,
    @SerializedName("teacherId") val teacherId: Long,
    @SerializedName("teacherName") val teacherName: String,
    @SerializedName("studentName") val studentName: String? = null,
    @SerializedName("channelName") val channelName: String,
    @SerializedName("orderPrice") val orderPrice: String? = null,
    @SerializedName("payAmount") val payAmount: String? = null,
    @SerializedName("payType") val payType: String? = null
)
