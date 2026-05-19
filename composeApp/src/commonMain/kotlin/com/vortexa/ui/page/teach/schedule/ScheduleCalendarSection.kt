package com.vortexa.ui.page.teach.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.page.teach.helper.TeachingCalendarDay
import com.vortexa.ui.page.teach.helper.TeachingDate
import com.vortexa.ui.page.teach.helper.teachingMonthGridDays
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.ui.theme.FontSemiBold
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.icon_back
import org.jetbrains.compose.resources.painterResource

private val weekdayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

/**
 * 日程页顶部日历模块。
 *
 * @param selectedDate 当前选中的日期，null 表示未选
 * @param onDateSelected 日期点击回调
 */
@Composable
fun ScheduleCalendarSection(
    selectedDate: TeachingDate?,
    onDateSelected: (TeachingDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonth = remember { TeachingDate.today().copy(day = 1) }
    val startMonth = remember { currentMonth.plusMonths(-12) }
    val endMonth = remember { currentMonth.plusMonths(12) }
    var visibleMonth by remember { mutableStateOf(currentMonth) }
    val monthDays = remember(visibleMonth) { teachingMonthGridDays(visibleMonth) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MonthArrow(
                contentDescription = "上一月",
                enabled = visibleMonth > startMonth,
                onClick = { visibleMonth = visibleMonth.plusMonths(-1) }
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = visibleMonth.monthName(),
                    style = FontSemiBold(fontSize = 20, color = Colors.black_101828)
                )
                Text(
                    text = visibleMonth.year.toString(),
                    style = FontMedium(fontSize = 12, color = Colors.black_101828)
                )
            }
            MonthArrow(
                contentDescription = "下一月",
                enabled = visibleMonth < endMonth,
                rotate = 180f,
                onClick = { visibleMonth = visibleMonth.plusMonths(1) }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekdayLabels.forEach { label ->
                Text(
                    modifier = Modifier.weight(1f),
                    text = label,
                    textAlign = TextAlign.Center,
                    style = FontRegular(fontSize = 12, color = Colors.gray_6A7282)
                )
            }
        }
        monthDays.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                week.forEach { day ->
                    ScheduleDay(
                        day = day,
                        isSelected = selectedDate == day.date,
                        onClick = { if (day.isCurrentMonth) onDateSelected(day.date) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthArrow(
    contentDescription: String,
    enabled: Boolean,
    rotate: Float = 0f,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .click { if (enabled) onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.icon_back),
            contentDescription = contentDescription,
            modifier = Modifier.size(34.dp).rotate(rotate),
            tint = if (enabled) Colors.black_101828 else Colors.gray_B1B8C6
        )
    }
}

@Composable
private fun ScheduleDay(
    day: TeachingCalendarDay,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = when {
        isSelected -> Color.White
        day.isCurrentMonth -> Colors.black_101828
        else -> Colors.gray_B1B8C6
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(10.dp)
            .background(
                color = if (isSelected) Colors.blue_3266FF else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .clickable(enabled = day.isCurrentMonth, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.day.toString(),
            style = FontMedium(fontSize = 14, color = textColor)
        )
    }
}

@Composable
@Preview
private fun ScheduleCalendarSectionPreview() {
    ScheduleCalendarSection(
        selectedDate = TeachingDate.today(),
        onDateSelected = {}
    )
}
