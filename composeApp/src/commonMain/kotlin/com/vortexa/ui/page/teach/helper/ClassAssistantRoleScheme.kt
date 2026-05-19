package com.vortexa.ui.page.teach.helper

/**
 * 路由中的角色参数，与后端约定：`teacher` | `student`。
 * 缺省时由 GET `/v/api/c2c/teacher/reserve/classroom` 的 [com.vortexa.model.ReserveClassroomDetail.counterpartRole] 在 [mapReserveClassroomToClassAssistantUi] 中推断。
 */
internal object ClassAssistantRoleScheme {

    const val VALUE_TEACHER = "teacher"
    const val VALUE_STUDENT = "student"

    /** @return 合法则返回枚举；未知或空白返回 null（走接口推断） */
    fun parse(raw: String?): ClassAssistantRole? {
        val v = raw?.trim()?.lowercase().orEmpty()
        if (v.isEmpty()) return null
        return when (v) {
            VALUE_TEACHER -> ClassAssistantRole.Tutor
            VALUE_STUDENT -> ClassAssistantRole.Student
            else -> null
        }
    }

    /** @return 是否为 Scheme 允许的角色字面量（用于路由校验） */
    fun isAllowedLiteral(raw: String?): Boolean {
        val v = raw?.trim()?.lowercase().orEmpty()
        if (v.isEmpty()) return true
        return v == VALUE_TEACHER || v == VALUE_STUDENT
    }
}
