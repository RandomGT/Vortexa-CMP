package com.vortexa.ui.page.teach.helper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

sealed interface ClassAssistantBottomBarUi {
    data object Hidden : ClassAssistantBottomBarUi

    data class TutorPending(
        val onMessage: () -> Unit,
        val onReject: () -> Unit,
        val onAccept: () -> Unit
    ) : ClassAssistantBottomBarUi

    /** 已接受且未开课：私信 + 取消预约（走拒绝接口） */
    data class TutorMessageAndCancel(
        val onMessage: () -> Unit,
        val onCancelReject: () -> Unit
    ) : ClassAssistantBottomBarUi

    data class StudentSingle(
        val label: String,
        val onClick: () -> Unit
    ) : ClassAssistantBottomBarUi
}

fun classAssistantBottomBarUi(
    state: ClassAssistantUiState,
    now: Long,
    onMessage: () -> Unit,
    onReject: () -> Unit,
    onAccept: () -> Unit,
    onCancelReserve: () -> Unit,
    onTutorCancelBeforeCourse: () -> Unit,
    onRebook: () -> Unit
): ClassAssistantBottomBarUi {
    val status = state.apiStatus.trim()
    return when (state.role) {
        ClassAssistantRole.Tutor -> when {
            ClassAssistantStatusSets.isPendingAccept(status) ->
                ClassAssistantBottomBarUi.TutorPending(
                    onMessage = onMessage,
                    onReject = onReject,
                    onAccept = onAccept
                )
            ClassAssistantStatusSets.isTutorMessageOnly(status) && now < state.courseStartEpochMilli ->
                ClassAssistantBottomBarUi.TutorMessageAndCancel(
                    onMessage = onMessage,
                    onCancelReject = onTutorCancelBeforeCourse
                )
            ClassAssistantStatusSets.isTutorMessageOnly(status) ->
                ClassAssistantBottomBarUi.StudentSingle("私信", onMessage)
            else -> ClassAssistantBottomBarUi.Hidden
        }
        ClassAssistantRole.Student -> when {
            ClassAssistantStatusSets.isTerminalNegative(status) ->
                ClassAssistantBottomBarUi.Hidden
            ClassAssistantStatusSets.isCompleted(status) ->
                ClassAssistantBottomBarUi.StudentSingle("重新预约", onRebook)
            ClassAssistantStatusSets.isPendingAccept(status) ||
            ClassAssistantStatusSets.isUpcoming(status) ->
                ClassAssistantBottomBarUi.StudentSingle("取消预约", onCancelReserve)
            ClassAssistantStatusSets.isPendingComplete(status) ->
                ClassAssistantBottomBarUi.Hidden
            else -> studentBottomBarLegacyByTime(state, now, onCancelReserve, onRebook)
        }
    }
}

/** 非上述约定文案时，按课程时间兜底（兼容旧 status） */
private fun studentBottomBarLegacyByTime(
    state: ClassAssistantUiState,
    now: Long,
    onCancelReserve: () -> Unit,
    onRebook: () -> Unit
): ClassAssistantBottomBarUi = when {
    now < state.courseStartEpochMilli ->
        ClassAssistantBottomBarUi.StudentSingle("取消预约", onCancelReserve)
    now >= state.courseEndEpochMilli ->
        ClassAssistantBottomBarUi.StudentSingle("重新预约", onRebook)
    else -> ClassAssistantBottomBarUi.Hidden
}

@Composable
fun ClassAssistantBottomBar(
    ui: ClassAssistantBottomBarUi,
    modifier: Modifier = Modifier,
    tutorActionsEnabled: Boolean = true
) {
    val rowShape = RoundedCornerShape(30.dp)
    when (ui) {
        ClassAssistantBottomBarUi.Hidden -> Unit
        is ClassAssistantBottomBarUi.TutorPending -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClassAssistantBottomCapsule(
                    text = "拒绝",
                    dark = false,
                    onClick = ui.onReject,
                    modifier = Modifier.weight(1f),
                    shape = rowShape,
                    enabled = tutorActionsEnabled
                )
                ClassAssistantBottomCapsule(
                    text = "接受",
                    dark = true,
                    onClick = ui.onAccept,
                    modifier = Modifier.weight(1f),
                    shape = rowShape,
                    enabled = tutorActionsEnabled
                )
            }
        }
        is ClassAssistantBottomBarUi.TutorMessageAndCancel -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClassAssistantBottomCapsule(
                    text = "私信",
                    dark = false,
                    onClick = ui.onMessage,
                    modifier = Modifier.weight(1f),
                    shape = rowShape,
                    enabled = tutorActionsEnabled
                )
                ClassAssistantBottomCapsule(
                    text = "取消预约",
                    dark = true,
                    onClick = ui.onCancelReject,
                    modifier = Modifier.weight(1f),
                    shape = rowShape,
                    enabled = tutorActionsEnabled
                )
            }
        }
        is ClassAssistantBottomBarUi.StudentSingle -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ClassAssistantBottomCapsule(
                    text = ui.label,
                    dark = true,
                    onClick = ui.onClick,
                    modifier = Modifier.weight(1f),
                    shape = rowShape
                )
            }
        }
    }
}

@Preview
@Composable
private fun ClassAssistantBottomBarTutorPreview() {
    ClassAssistantBottomBar(
        ClassAssistantBottomBarUi.TutorPending({}, {}, {})
    )
}
