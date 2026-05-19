package com.vortexa.ui.page.teach.helper

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vortexa.model.ReserveListApiStatus
import com.vortexa.ui.theme.Colors

enum class ReserveCancelHeroDisplay {
    StudentInitiatedCancel,
    StudentSelfCancelled,
    TutorInitiatedCancelStudentView,
    TutorSelfCancelled,
    GenericCancelled,
}

fun isReserveStatusCancelled(status: String): Boolean {
    val s = status.trim()
    if (s.equals("CANCELLED", ignoreCase = true)) return true
    if (s.equals(ReserveListApiStatus.CANCELED, ignoreCase = true)) return true
    return s == "已取消"
}

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
        val byOperator = when (opNorm) {
            "student", "learner" -> if (viewerIsStudent) {
                ReserveCancelHeroDisplay.StudentSelfCancelled
            } else {
                ReserveCancelHeroDisplay.StudentInitiatedCancel
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
    val roleMeansStudent = roleNorm == "student" || roleNorm == "learner" || roleNorm == "学员"
    val roleMeansTeacher = roleNorm == "teacher" || roleNorm == "tutor" || roleNorm == "导师"
    val byStudent = when {
        cancelUserId != null && studentUserId > 0L && cancelUserId == studentUserId -> true
        cancelUserId != null && teacherUserId > 0L && cancelUserId == teacherUserId -> false
        roleMeansStudent -> true
        roleMeansTeacher -> false
        else -> null
    }
    return when (byStudent) {
        true -> if (viewerIsStudent) ReserveCancelHeroDisplay.StudentSelfCancelled else ReserveCancelHeroDisplay.StudentInitiatedCancel
        false -> if (viewerIsStudent) ReserveCancelHeroDisplay.TutorInitiatedCancelStudentView else ReserveCancelHeroDisplay.TutorSelfCancelled
        null -> if (!viewerIsStudent) ReserveCancelHeroDisplay.StudentInitiatedCancel else ReserveCancelHeroDisplay.GenericCancelled
    }
}

@Composable
fun ReserveCancelHeroMessage(
    display: ReserveCancelHeroDisplay,
    modifier: Modifier = Modifier
) {
    val black = SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp)
    val blue = SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp)
    val text = buildAnnotatedString {
        when (display) {
            ReserveCancelHeroDisplay.StudentInitiatedCancel,
            ReserveCancelHeroDisplay.StudentSelfCancelled -> {
                pushStyle(black)
                append("学员")
                pop()
                pushStyle(blue)
                append("已取消")
                pop()
                pushStyle(black)
                append("该预约")
            }
            ReserveCancelHeroDisplay.TutorInitiatedCancelStudentView -> {
                pushStyle(black)
                append("导师")
                pop()
                pushStyle(blue)
                append("已拒绝")
                pop()
                pushStyle(black)
                append("该预约课程")
            }
            ReserveCancelHeroDisplay.TutorSelfCancelled -> {
                pushStyle(black)
                append("导师")
                pop()
                pushStyle(blue)
                append("已拒绝")
                pop()
                pushStyle(black)
                append("该预约")
            }
            ReserveCancelHeroDisplay.GenericCancelled -> {
                pushStyle(black)
                append("该预约已")
                pop()
                pushStyle(blue)
                append("取消")
            }
        }
    }
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 24.dp),
        textAlign = TextAlign.Center,
        lineHeight = 32.sp
    )
}

@Preview
@Composable
private fun ReserveCancelHeroMessagePreview() {
    ReserveCancelHeroMessage(display = ReserveCancelHeroDisplay.StudentInitiatedCancel)
}
