package com.vortexa.ui.page.teach.helper

import com.vortexa.config.UserConfig
import com.vortexa.model.ReserveClassroomDetail
import com.vortexa.model.ReserveDetail
import com.vortexa.ui.page.teach.resolveReserveCancelHeroDisplay
import com.vortexa.ui.page.teach.order.one2one.parseCourseStartEpochMilli

/**
 * 由 [ReserveDetail] 映射为课堂小助手 UI 状态。
 *
 * @param roleOverride Scheme / 入口传入的视角角色；非 null 时**优先**于根据当前用户与 `userId`/`teacherId` 的推断。
 * 推断规则：`userId` → 学员，`teacherId` → 导师；均不匹配时默认学员。
 */
fun mapReserveDetailToClassAssistantUi(
    d: ReserveDetail,
    roleOverride: ClassAssistantRole? = null
): ClassAssistantUiState {
    val myId = UserConfig.getUserId()
    val role = roleOverride ?: when {
        myId != 0L && myId == d.userId -> ClassAssistantRole.Student
        myId != 0L && myId == d.teacherId -> ClassAssistantRole.Tutor
        else -> ClassAssistantRole.Student
    }
    val statusNorm = d.status.trim()
    val tutorDecision = when {
        ClassAssistantStatusSets.isTerminalNegative(statusNorm) -> TutorBookingDecision.Rejected
        ClassAssistantStatusSets.isPendingAccept(statusNorm) -> TutorBookingDecision.Pending
        else -> TutorBookingDecision.Accepted
    }
    val start = parseCourseStartEpochMilli(d.courseStartTime)
        ?: System.currentTimeMillis()
    val hours = d.hour.coerceAtLeast(1)
    val end = start + hours * 60L * 60L * 1000L
    val counterpartyLabel = if (role == ClassAssistantRole.Tutor) "学员" else "导师"
    val counterpartyDisplayText = if (role == ClassAssistantRole.Tutor) {
        d.studentName?.trim()?.takeIf { it.isNotEmpty() } ?: d.userId.toString()
    } else {
        d.teacherName.trim().takeIf { it.isNotEmpty() } ?: d.teacherId.toString()
    }
    val durationText = if (d.hour == 1) "1小时" else "${d.hour}小时"
    val cancelHero = resolveReserveCancelHeroDisplay(
        d.status,
        myId,
        d.userId,
        d.teacherId,
        d.cancelUserId,
        d.cancelRole,
        d.cancelOperator,
    )
    return ClassAssistantUiState(
        reserveId = d.reserveId.toInt(),
        apiStatus = d.status,
        role = role,
        tutorDecision = tutorDecision,
        courseStartEpochMilli = start,
        courseEndEpochMilli = end,
        counterpartyLabel = counterpartyLabel,
        counterpartyDisplayText = counterpartyDisplayText,
        reserveTimeText = d.reserveCreateTime,
        oneOnOneTimeText = d.courseStartTime,
        durationText = durationText,
        teacherIdForRebook = d.teacherId,
        cancelHeroDisplay = cancelHero
    )
}

/**
 * 由 [ReserveClassroomDetail] 映射为课堂小助手 UI 状态（GET /v/api/c2c/teacher/reserve/classroom）。
 *
 * @param reserveId 入口预约 ID（本接口 data 不含 reserveId，由路由传入）
 * @param roleOverride 与 [mapReserveDetailToClassAssistantUi] 相同，优先于由 [counterpartRole] 推断：
 * 对方为 `teacher` 则当前用户视为学员，对方为 `student` 则当前用户视为导师。
 */
/**
 * 接口 HTTP 成功但 data 为无意义空壳（如 `{}` 反序列化结果）时，用空态页代替详情。
 */
fun ReserveClassroomDetail.isClassAssistantEmptyPayload(): Boolean =
    counterpartUserId == 0L && status.isBlank() && counterpartRole.isBlank()

fun mapReserveClassroomToClassAssistantUi(
    reserveId: Int,
    d: ReserveClassroomDetail,
    roleOverride: ClassAssistantRole? = null
): ClassAssistantUiState {
    val myId = UserConfig.getUserId()
    val roleNorm = d.counterpartRole.trim().lowercase()
    val counterpartIsTeacher = roleNorm == "teacher"
    val counterpartIsStudent = roleNorm == "student"
    val inferredRole = when {
        counterpartIsTeacher -> ClassAssistantRole.Student
        counterpartIsStudent -> ClassAssistantRole.Tutor
        else -> ClassAssistantRole.Student
    }
    val role = roleOverride ?: inferredRole
    val statusNorm = d.status.trim()
    val tutorDecision = when {
        ClassAssistantStatusSets.isTerminalNegative(statusNorm) -> TutorBookingDecision.Rejected
        ClassAssistantStatusSets.isPendingAccept(statusNorm) -> TutorBookingDecision.Pending
        else -> TutorBookingDecision.Accepted
    }
    val start = parseCourseStartEpochMilli(d.courseStartTime)
        ?: System.currentTimeMillis()
    val hours = d.hour.coerceAtLeast(1)
    val end = start + hours * 60L * 60L * 1000L
    val counterpartyLabel = if (role == ClassAssistantRole.Tutor) "学员" else "导师"
    val counterpartyDisplayText = d.counterpartUserName?.trim()?.takeIf { it.isNotEmpty() }
        ?: d.counterpartUserId.toString()
    val durationText = if (d.hour == 1) "1小时" else "${d.hour}小时"
    val teacherIdForRebook = when {
        role == ClassAssistantRole.Student && counterpartIsTeacher -> d.counterpartUserId
        else -> 0L
    }
    val studentUserId = when {
        counterpartIsStudent -> d.counterpartUserId
        counterpartIsTeacher -> myId
        else -> 0L
    }
    val teacherUserId = when {
        counterpartIsTeacher -> d.counterpartUserId
        counterpartIsStudent -> myId
        else -> 0L
    }
    val cancelHero = resolveReserveCancelHeroDisplay(
        d.status,
        myId,
        studentUserId,
        teacherUserId,
        d.cancelUserId,
        d.cancelRole,
        d.cancelOperator,
    )
    return ClassAssistantUiState(
        reserveId = reserveId,
        apiStatus = d.status,
        role = role,
        tutorDecision = tutorDecision,
        courseStartEpochMilli = start,
        courseEndEpochMilli = end,
        counterpartyLabel = counterpartyLabel,
        counterpartyDisplayText = counterpartyDisplayText,
        reserveTimeText = d.reserveCreateTime,
        oneOnOneTimeText = d.courseStartTime,
        durationText = durationText,
        teacherIdForRebook = teacherIdForRebook,
        cancelHeroDisplay = cancelHero
    )
}
