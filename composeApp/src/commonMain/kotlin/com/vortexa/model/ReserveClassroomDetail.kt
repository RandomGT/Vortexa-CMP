package com.vortexa.model

import com.google.gson.annotations.SerializedName

/**
 * 课堂小助手详情接口 GET /v/api/c2c/teacher/reserve/classroom 的 data 结构。
 *
 * @param counterpartRole 对方身份：`teacher` / `student`
 * @param counterpartUserName 对方用户昵称/姓名（与 [counterpartUserId] 对应）
 * @param status 预约状态，如 `TO_ACCEPT`（未接受）、`REJECTED`（已拒绝），或与旧版一致的中文文案
 * @param cancelUserId 取消操作人用户 ID（可选）
 * @param cancelRole 取消方：`student` / `teacher` 等（可选）
 * @param cancelOperator 取消操作方：`student` / `teacher`（可选，与详情接口一致）
 */
data class ReserveClassroomDetail(
    @SerializedName("status") val status: String,
    @SerializedName("counterpartUserId") val counterpartUserId: Long,
    @SerializedName(value = "counterpartUserName", alternate = ["counterpartName", "userName", "nickName"])
    val counterpartUserName: String? = null,
    @SerializedName("counterpartRole") val counterpartRole: String,
    @SerializedName("reserveCreateTime") val reserveCreateTime: String,
    @SerializedName("courseStartTime") val courseStartTime: String,
    @SerializedName("hour") val hour: Int,
    @SerializedName(value = "cancelUserId", alternate = ["cancelByUserId"])
    val cancelUserId: Long? = null,
    @SerializedName(value = "cancelRole", alternate = ["cancelByRole", "cancelledByRole", "cancelUserRole"])
    val cancelRole: String? = null,
    @SerializedName("cancelOperator") val cancelOperator: String? = null,
)
