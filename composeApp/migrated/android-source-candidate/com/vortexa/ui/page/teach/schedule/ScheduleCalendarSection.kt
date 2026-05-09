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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.ui.theme.FontSemiBold
import com.vortexa.util.extension.click
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import vortexa.composeapp.generated.resources.Res

/**
 * 日程页顶部日历模块。
 * 使用 kizitonwose/Calendar 实现月视图，支持单日选中，样式与项目主题一致。
 *
 * @param selectedDate 当前选中的日期，null 表示未选
 * @param onDateSelected 日期点击回调
 */
@Composable
fun ScheduleCalendarSection(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(12) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }
    val daysOfWeek = remember { daysOfWeek(firstDayOfWeek = firstDayOfWeek) }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )
    val scope = rememberCoroutineScope()
    // firstVisibleMonth 在 Android compose 中为 java.time.YearMonth
    val visibleYearMonth: YearMonth = state.firstVisibleMonth.yearMonth

    Column(modifier = modifier.fillMaxWidth()) {
        // 月份切换器（Figma 283-30259）：左箭头 | 月份+年份 | 右箭头
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .click {
                        val target = visibleYearMonth.minusMonths(1)
                        if (target >= startMonth) {
                            scope.launch {
                                state.animateScrollToMonth(target)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.icon_back),
                    contentDescription = "上一月",
                    modifier = Modifier.size(34.dp),
                    tint = Colors.black_101828
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = visibleYearMonth.month.getDisplayName(
                        TextStyle.FULL,
                        Locale.ENGLISH
                    ),
                    style = FontSemiBold(fontSize = 20, color = Colors.black_101828)
                )
                Text(
                    text = visibleYearMonth.year.toString(),
                    style = FontMedium(fontSize = 12, color = Colors.black_101828)
                )
            }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .click {
                        val target = visibleYearMonth.plusMonths(1)
                        if (target <= endMonth) {
                            scope.launch {
                                state.animateScrollToMonth(target)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.icon_back),
                    contentDescription = "下一月",
                    modifier = Modifier.size(34.dp).rotate(180f),
                    tint = Colors.black_101828
                )
            }
        }
        // 星期标题行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            daysOfWeek.forEach { dayOfWeek ->
                Text(
                    modifier = Modifier.weight(1f),
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    textAlign = TextAlign.Center,
                    style = FontRegular(fontSize = 12, color = Colors.gray_6A7282)
                )
            }
        }
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                ScheduleDay(
                    day = day,
                    isSelected = selectedDate == day.date,
                    onClick = {
                        if (day.position == DayPosition.MonthDate) {
                            onDateSelected(day.date)
                        }
                    }
                )
            },
            userScrollEnabled = true
        )
    }
}

/**
 * 单日格子：本月日期可点击，选中显示蓝色圆底；非本月日期灰色。
 */
@Composable
private fun ScheduleDay(
    day: CalendarDay,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isMonthDate = day.position == DayPosition.MonthDate
    val textColor = when {
        isSelected -> Color.White
        isMonthDate -> Colors.black_101828
        else -> Colors.gray_B1B8C6
    }
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(10.dp)
            .background(
                color = if (isSelected) Colors.blue_3266FF else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .clickable(enabled = isMonthDate, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = FontMedium(fontSize = 14, color = textColor)
        )
    }
}

@Composable
@androidx.compose.ui.tooling.preview.Preview
private fun ScheduleCalendarSectionPreview() {
    ScheduleCalendarSection(
        selectedDate = LocalDate.now(),
        onDateSelected = {}
    )
}
