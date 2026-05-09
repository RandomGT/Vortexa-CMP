package com.vortexa.model

/**
 * 导师某天某时段预约时间项，对应接口 /v/api/c2c/teacher/reserve/time 单条 data。
 *
 * @param reserveHour 时段文案，如 "18:00-19:00"
 * @param canReserve 该时段是否可预约
 */
data class TeacherReserveTimeItem(
    val reserveHour: String,
    val canReserve: Boolean
)
