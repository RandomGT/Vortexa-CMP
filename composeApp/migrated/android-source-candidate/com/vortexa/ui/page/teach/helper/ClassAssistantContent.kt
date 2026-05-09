package com.vortexa.ui.page.teach.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ClassAssistantContent(
    ui: ClassAssistantUiState,
    bottomUi: ClassAssistantBottomBarUi,
    onBackClick: () -> Unit,
    onSupportClick: () -> Unit,
    tutorActionsEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        ClassAssistantToolbar(
            onBackClick = onBackClick,
            onSupportClick = onSupportClick
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            ClassAssistantHeroMessage(state = ui)
            ClassAssistantDetailSection(state = ui)
        }
        ClassAssistantBottomBar(
            ui = bottomUi,
            tutorActionsEnabled = tutorActionsEnabled
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ClassAssistantContentPreview() {
    val ui = ClassAssistantUiState(
        reserveId = 1,
        apiStatus = "待导师确认",
        role = ClassAssistantRole.Tutor,
        tutorDecision = TutorBookingDecision.Pending,
        courseStartEpochMilli = System.currentTimeMillis() + 86_400_000L,
        courseEndEpochMilli = System.currentTimeMillis() + 88_400_000L,
        counterpartyLabel = "学员",
        counterpartyDisplayText = "张三",
        reserveTimeText = "2025-10-08 16:00",
        oneOnOneTimeText = "2025-10-08 16:00",
        durationText = "2小时",
        teacherIdForRebook = 1L
    )
    ClassAssistantContent(
        ui = ui,
        bottomUi = classAssistantBottomBarUi(
            state = ui,
            now = System.currentTimeMillis(),
            onMessage = {},
            onReject = {},
            onAccept = {},
            onCancelReserve = {},
            onTutorCancelBeforeCourse = {},
            onRebook = {}
        ),
        onBackClick = {},
        onSupportClick = {}
    )
}
