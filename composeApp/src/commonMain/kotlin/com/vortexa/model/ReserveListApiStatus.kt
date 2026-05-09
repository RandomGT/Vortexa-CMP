package com.vortexa.model

/**
 * 一对一预约在接口中的 **status** 约定值（如 `GET /v/api/c2c/teacher/reserve/list` 的查询参数及列表项 [ReserveListItem.status]）。
 * 映射中文等规则应优先使用本处常量，避免字面量散落。
 */
object ReserveListApiStatus {
    const val TO_ACCEPT = "TO_ACCEPT"
    const val TO_START = "TO_START"
    const val REJECTED = "REJECTED"
    const val CANCELED = "CANCELED"
    const val COMPLETED = "COMPLETED"

    /** 接口文档约定的五种 status（序列化通常为全大写，比较时建议对入参做 `uppercase()`） */
    val ALL: Set<String> = setOf(TO_ACCEPT, TO_START, REJECTED, CANCELED, COMPLETED)
}
