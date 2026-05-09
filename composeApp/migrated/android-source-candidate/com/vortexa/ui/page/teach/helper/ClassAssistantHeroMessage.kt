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
import com.vortexa.ui.page.teach.ReserveCancelHeroMessage
import com.vortexa.ui.theme.Colors

private fun pendingHeroHighlight(status: String): String =
    if (status.trim().equals(ReserveListApiStatus.TO_ACCEPT, ignoreCase = true)) "未接受"
    else "待接受"

@Composable
fun ClassAssistantHeroMessage(
    state: ClassAssistantUiState,
    modifier: Modifier = Modifier
) {
    state.cancelHeroDisplay?.let { display ->
        ReserveCancelHeroMessage(display = display, modifier = modifier)
        return
    }
    val status = state.apiStatus.trim()
    val text = buildAnnotatedString {
        when (state.role) {
            ClassAssistantRole.Tutor -> when (state.tutorDecision) {
                TutorBookingDecision.Pending -> {
                    pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                    append("您有一单")
                    append("\n")
                    pop()
                    pushStyle(SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                    append(pendingHeroHighlight(status))
                    pop()
                    pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                    append("的预约课程")
                }
                TutorBookingDecision.Accepted -> when {
                    ClassAssistantStatusSets.isUpcoming(status) -> {
                        pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                        append("课程")
                        pop()
                        pushStyle(SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                        append("即将开始")
                    }
                    ClassAssistantStatusSets.isPendingComplete(status) -> {
                        pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                        append("课程")
                        pop()
                        pushStyle(SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                        append("进行中")
                    }
                    ClassAssistantStatusSets.isCompleted(status) -> {
                        pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                        append("本次课程")
                        pop()
                        pushStyle(SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                        append("已完成")
                    }
                    else -> {
                        pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                        append("您已")
                        pop()
                        pushStyle(SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                        append("接受")
                        pop()
                        pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                        append("该预约课程")
                    }
                }
                TutorBookingDecision.Rejected -> {
                    when {
                        ClassAssistantStatusSets.isCancelled(status) -> {
                            pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                            append("该预约已")
                            pop()
                            pushStyle(SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                            append("取消")
                        }
                        status.equals(ReserveListApiStatus.REJECTED, ignoreCase = true) -> {
                            pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                            append("您")
                            pop()
                            pushStyle(SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                            append(classAssistantApiStatusDisplayZh(status))
                            pop()
                            pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                            append("该预约课程")
                        }
                        ClassAssistantStatusSets.isRejected(status) -> {
                            pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                            append("您已")
                            pop()
                            pushStyle(SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                            append("拒绝")
                            pop()
                            pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                            append("该预约课程")
                        }
                        else -> {
                            pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                            append("您已")
                            pop()
                            pushStyle(SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                            append("拒绝")
                            pop()
                            pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                            append("该预约课程")
                        }
                    }
                }
            }
            ClassAssistantRole.Student -> when {
                ClassAssistantStatusSets.isCancelled(status) -> {
                    pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                    append("该预约已")
                    pop()
                    pushStyle(SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                    append("取消")
                }
                status.equals(ReserveListApiStatus.REJECTED, ignoreCase = true) -> {
                    pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                    append("该预约")
                    pop()
                    pushStyle(SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                    append(classAssistantApiStatusDisplayZh(status))
                }
                ClassAssistantStatusSets.isRejected(status) -> {
                    pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                    append("预约已被")
                    pop()
                    pushStyle(SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                    append("拒绝")
                }
                ClassAssistantStatusSets.isCompleted(status) -> {
                    pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                    append("本次课程")
                    pop()
                    pushStyle(SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                    append("已完成")
                }
                ClassAssistantStatusSets.isPendingAccept(status) -> {
                    pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                    append("预约")
                    pop()
                    pushStyle(SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                    append(pendingHeroHighlight(status))
                    pop()
                    pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                    append("\n请等待导师确认")
                }
                ClassAssistantStatusSets.isUpcoming(status) -> {
                    pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                    append("课程")
                    pop()
                    pushStyle(SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                    append("即将开始")
                }
                ClassAssistantStatusSets.isPendingComplete(status) -> {
                    pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                    append("课程")
                    pop()
                    pushStyle(SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                    append("进行中")
                }
                else -> {
                    val now = System.currentTimeMillis()
                    if (now < state.courseStartEpochMilli) {
                        pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                        append("您有一节")
                        pop()
                        pushStyle(SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                        append("进行中")
                        pop()
                        pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                        append("\n的预约课程")
                    } else if (now >= state.courseEndEpochMilli) {
                        pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                        append("本次课程")
                        pop()
                        pushStyle(SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                        append("已结束")
                    } else {
                        pushStyle(SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                        append("课程")
                        pop()
                        pushStyle(SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp))
                        append("进行中")
                    }
                }
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
private fun ClassAssistantHeroMessagePreview() {
    ClassAssistantHeroMessage(
        state = ClassAssistantUiState(
            reserveId = 1,
            apiStatus = "待导师确认",
            role = ClassAssistantRole.Tutor,
            tutorDecision = TutorBookingDecision.Pending,
            courseStartEpochMilli = 0L,
            courseEndEpochMilli = 0L,
            counterpartyLabel = "学员",
            counterpartyDisplayText = "张三",
            reserveTimeText = "",
            oneOnOneTimeText = "",
            durationText = "2小时",
            teacherIdForRebook = 0L
        )
    )
}
