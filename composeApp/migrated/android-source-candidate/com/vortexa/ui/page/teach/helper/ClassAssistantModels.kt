package com.vortexa.ui.page.teach.helper

import com.vortexa.ui.page.teach.ReserveCancelHeroDisplay

/** 当前用户在本页的角色（由预约详情中的 userId / teacherId 与登录用户比对） */
enum class ClassAssistantRole {
    Tutor,
    Student
}

/** 导师对待处理预约的受理状态（由接口 [status] 映射） */
enum class TutorBookingDecision {
    Pending,
    Accepted,
    Rejected
}

/**
 * 课堂小助手页展示数据
 *
 * @param reserveId 预约 ID，用于取消预约等请求
 * @param apiStatus 接口原始状态：`TO_ACCEPT` / `REJECTED` 或旧版中文；UI 展示时会将英文码译为中文
 */
data class ClassAssistantUiState(
    val reserveId: Int,
    val apiStatus: String,
    val role: ClassAssistantRole,
    val tutorDecision: TutorBookingDecision,
    val courseStartEpochMilli: Long,
    val courseEndEpochMilli: Long,
    val counterpartyLabel: String,
    /** 学员 / 导师行右侧展示：优先对方昵称/姓名，缺失时回退为 userId 文本 */
    val counterpartyDisplayText: String,
    val reserveTimeText: String,
    val oneOnOneTimeText: String,
    val durationText: String,
    val teacherIdForRebook: Long,
    /** 已取消预约时顶部归因文案；非取消为 null */
    val cancelHeroDisplay: ReserveCancelHeroDisplay? = null
)
