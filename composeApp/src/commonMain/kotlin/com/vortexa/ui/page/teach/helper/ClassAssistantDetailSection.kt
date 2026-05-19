package com.vortexa.ui.page.teach.helper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.ui.theme.FontSemiBold

@Composable
fun ClassAssistantDetailSection(
    state: ClassAssistantUiState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "详情",
            style = FontSemiBold(fontSize = 18, color = Colors.black_101828),
            modifier = Modifier
                .padding(horizontal = 18.dp)
                .padding(top = 24.dp, bottom = 6.dp)
        )
        ClassAssistantDetailRow(
            label = state.counterpartyLabel,
            trailingContent = {
                Text(
                    text = state.counterpartyDisplayText,
                    style = FontSemiBold(fontSize = 15, color = Colors.black_101828),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        )
        ClassAssistantDetailRow(
            label = "预约时间",
            value = state.reserveTimeText
        )
        ClassAssistantDetailRow(
            label = "一对一指导时间",
            value = state.oneOnOneTimeText
        )
        ClassAssistantDetailRow(
            label = "指导时长",
            value = state.durationText
        )
    }
}

@Composable
private fun ClassAssistantDetailRow(
    label: String,
    value: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = FontRegular(fontSize = 15, color = Colors.gray_6A7282),
            modifier = Modifier.weight(1f)
        )
        if (trailingContent != null) {
            trailingContent()
        } else if (value != null) {
            Text(
                text = value,
                style = FontSemiBold(fontSize = 15, color = Colors.black_101828)
            )
        }
    }
}

@Preview
@Composable
private fun ClassAssistantDetailSectionPreview() {
    ClassAssistantDetailSection(
        state = ClassAssistantUiState(
            reserveId = 1,
            apiStatus = "进行中",
            role = ClassAssistantRole.Tutor,
            tutorDecision = TutorBookingDecision.Pending,
            courseStartEpochMilli = 0L,
            courseEndEpochMilli = 0L,
            counterpartyLabel = "学员",
            counterpartyDisplayText = "张三",
            reserveTimeText = "2025-10-08 16:00",
            oneOnOneTimeText = "2025-10-08 16:00",
            durationText = "2小时",
            teacherIdForRebook = 0L
        )
    )
}
