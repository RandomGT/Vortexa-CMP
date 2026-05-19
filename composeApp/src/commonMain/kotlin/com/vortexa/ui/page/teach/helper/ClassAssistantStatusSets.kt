package com.vortexa.ui.page.teach.helper

import com.vortexa.model.ReserveListApiStatus

/**
 * 预约详情 [com.vortexa.model.ReserveDetail.status] 约定文案（及兼容旧接口同义状态）。
 * 英文码与 [ReserveListApiStatus] 对齐；旧中文枚举并存。
 * 见 [mapReserveDetailToClassAssistantUi]、[mapReserveClassroomToClassAssistantUi]、[classAssistantBottomBarUi]、[ClassAssistantHeroMessage]。
 */
internal object ClassAssistantStatusSets {

    /** 待导师接受：私信 / 拒绝 / 接受；学员可取消预约 */
    val pendingAccept = setOf(
        ReserveListApiStatus.TO_ACCEPT,
        "待接受",
        "待导师确认",
        "待处理",
        "未接受",
        "待确认",
    )

    /** 已接受、开课前后：导师仅私信；学员可取消（至开课前） */
    val upcoming = setOf("即将开始", "进行中", "待完成")

    /** 上课中：学员无底部操作；导师仅私信 */
    val pendingComplete = setOf("进行中", "待完成")

    val completed = setOf("已完成")

    val rejected = setOf(ReserveListApiStatus.REJECTED, "已拒绝", "拒绝")

    val cancelled = setOf("已取消")

    /** 与 [isReserveStatusCancelled] 对齐的英文取消码（详情 status 可能为枚举串） */
    private val cancelledEnglishNorm = setOf("cancelled", "canceled")

    /** 负面终态 */
    val terminalNegative = rejected + cancelled

    /** 导师展示「私信」单行（即将开始 / 进行中） */
    val tutorMessageOnly = upcoming + pendingComplete

    fun isPendingAccept(status: String): Boolean {
        val s = status.trim()
        if (s.equals(ReserveListApiStatus.TO_ACCEPT, ignoreCase = true)) return true
        return s in pendingAccept
    }

    fun isRejected(status: String): Boolean {
        val s = status.trim()
        if (s.equals(ReserveListApiStatus.REJECTED, ignoreCase = true)) return true
        return s in rejected
    }

    fun isCancelled(status: String): Boolean {
        val s = status.trim()
        if (s in cancelled) return true
        return s.lowercase() in cancelledEnglishNorm
    }

    fun isTerminalNegative(status: String): Boolean = isRejected(status) || isCancelled(status)

    fun isCompleted(status: String): Boolean = status.trim() in completed

    fun isUpcoming(status: String): Boolean = status.trim() in upcoming

    fun isPendingComplete(status: String): Boolean = status.trim() in pendingComplete

    fun isTutorMessageOnly(status: String): Boolean =
        isUpcoming(status) || isPendingComplete(status)
}

/** 英文 status 码在界面上的中文展示；非约定英文码或非英文仍返回 trimmed 原文（含旧版中文 status）。 */
internal fun classAssistantApiStatusDisplayZh(raw: String): String {
    val s = raw.trim()
    return when {
        s.equals(ReserveListApiStatus.REJECTED, ignoreCase = true) -> "已拒绝"
        s.equals(ReserveListApiStatus.TO_ACCEPT, ignoreCase = true) -> "未接受"
        else -> s
    }
}
