package com.vortexa.ui.page.teach

import com.vortexa.model.ReserveListApiStatus

/**
 * 预约已取消时，订单详情 / 课堂小助手顶部文案样式（与发起取消方一致时使用固定句式）。
 */
enum class ReserveCancelHeroDisplay {
    /** 学员发起取消，当前为导师视角：「学员已取消该预约」 */
    StudentInitiatedCancel,

    /** 学员发起取消，当前为学员本人：「学员已取消该预约」 */
    StudentSelfCancelled,

    /** 导师发起取消，当前为学员视角：「导师已拒绝该预约课程」 */
    TutorInitiatedCancelStudentView,

    /** 导师发起取消，当前为导师本人：「导师已拒绝该预约」 */
    TutorSelfCancelled,

    /** 学员视角且无法判定取消方时 */
    GenericCancelled,
}

fun isReserveStatusCancelled(status: String): Boolean {
    val s = status.trim()
    if (s.equals("CANCELLED", ignoreCase = true)) return true
    if (s.equals(ReserveListApiStatus.CANCELED, ignoreCase = true)) return true
    return s == "已取消"
}

/**
 * @param viewerUserId 当前登录用户 ID
 * @param studentUserId 学员用户 ID
 * @param teacherUserId 导师用户 ID
 * @param cancelUserId 接口返回的取消操作人（若提供则优先于 [cancelRole]）
 * @param cancelRole 接口返回的取消方：`student` / `teacher` / `tutor` 等
 * @param cancelOperator 接口返回的取消操作方：`student` / `teacher`；非空时优先于 [cancelUserId] / [cancelRole]
 */
fun resolveReserveCancelHeroDisplay(
    status: String,
    viewerUserId: Long,
    studentUserId: Long,
    teacherUserId: Long,
    cancelUserId: Long?,
    cancelRole: String?,
    cancelOperator: String? = null,
): ReserveCancelHeroDisplay? {
    if (!isReserveStatusCancelled(status)) return null
    val viewerIsStudent = viewerUserId > 0L && viewerUserId == studentUserId
    val viewerIsTeacher = viewerUserId > 0L && viewerUserId == teacherUserId

    val opNorm = cancelOperator?.trim()?.lowercase().orEmpty()
    if (opNorm.isNotEmpty()) {
        val byOperator: ReserveCancelHeroDisplay? = when (opNorm) {
            "student", "learner" -> when {
                viewerIsStudent -> ReserveCancelHeroDisplay.StudentSelfCancelled
                else -> ReserveCancelHeroDisplay.StudentInitiatedCancel
            }
            "teacher", "tutor" -> when {
                viewerIsStudent -> ReserveCancelHeroDisplay.TutorInitiatedCancelStudentView
                viewerIsTeacher -> ReserveCancelHeroDisplay.TutorSelfCancelled
                else -> null
            }
            else -> null
        }
        if (byOperator != null) return byOperator
    }

    val roleNorm = cancelRole?.trim()?.lowercase().orEmpty()
    val roleMeansStudent =
        roleNorm == "student" || roleNorm == "learner" || roleNorm == "学员"
    val roleMeansTeacher =
        roleNorm == "teacher" || roleNorm == "tutor" || roleNorm == "导师"
    // cancelUserId 与学员/导师 ID 一致时优先于 cancelRole，避免服务端 role 与操作人不一致时导师端误显示「导师已拒绝该预约」
    val byStudent: Boolean? = when {
        cancelUserId != null && studentUserId > 0L && cancelUserId == studentUserId -> true
        cancelUserId != null && teacherUserId > 0L && cancelUserId == teacherUserId -> false
        roleMeansStudent -> true
        roleMeansTeacher -> false
        else -> null
    }
    return when (byStudent) {
        true -> when {
            viewerIsStudent -> ReserveCancelHeroDisplay.StudentSelfCancelled
            else -> ReserveCancelHeroDisplay.StudentInitiatedCancel
        }
        false -> when {
            viewerIsStudent -> ReserveCancelHeroDisplay.TutorInitiatedCancelStudentView
            else -> ReserveCancelHeroDisplay.TutorSelfCancelled
        }
        null -> when {
            !viewerIsStudent -> ReserveCancelHeroDisplay.StudentInitiatedCancel
            else -> ReserveCancelHeroDisplay.GenericCancelled
        }
    }
}
