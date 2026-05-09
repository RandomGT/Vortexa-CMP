package com.vortexa.model

/**
 * 预约一对一指导请求体，对应 POST /v/api/c2c/teacher/reserve。
 *
 * @param teacherId 导师 ID，必填
 * @param reserveDate 预约日期，格式 yyyy/MM/dd，必填
 * @param reserveHour 时段，如 18:00-19:00，必填
 * @param userId 预约用户 ID，必填
 */
data class TeacherReserveRequest(
    val teacherId: Int,
    val reserveDate: String,
    val reserveHour: String,
    val userId: Long
)

/**
 * 预约成功返回的回执数据。
 *
 * @param reserveId 预约订单 ID
 * @param status 状态，如 "进行中"
 * @param reserveCreateTime 预约创建时间
 * @param courseStartTime 课程开始时间
 * @param hour 课时（小时）
 * @param orderPrice 订单价格（暂缓）
 * @param payAmount 实付金额（暂缓）
 * @param payType 支付类型（暂缓），如 "积分"
 */
data class TeacherReserveReceipt(
    val reserveId: Long,
    val status: String,
    val reserveCreateTime: String,
    val courseStartTime: String,
    val hour: Int,
    val orderPrice: String? = null,
    val payAmount: String? = null,
    val payType: String? = null
)

/**
 * 取消预约请求体，对应 POST /v/api/c2c/teacher/reserve/cancel。
 *
 * @param reserveId 预约 ID，必填
 * @param reason 取消原因，必填
 */
data class ReserveCancelRequest(
    val reserveId: Int,
    val reason: String
)

/**
 * 导师接受预约，对应 POST /v/api/c2c/teacher/reserve/accept。
 */
data class ReserveAcceptRequest(
    val reserveId: Int
)

/**
 * 导师拒绝预约，对应 POST /v/api/c2c/teacher/reserve/reject。
 *
 * @param reason 拒绝原因，选填
 */
data class ReserveRejectRequest(
    val reserveId: Int,
    val reason: String? = null
)
