package com.vortexa.ui.page.teach.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.ui.theme.FontSemiBold
import com.vortexa.ui.page.teach.helper.TeachingDate

/**
 * 时间槽展示项（Figma 283-30279 Filter Chips）。
 * @param label 展示文案，如 "18:00~19:00"
 * @param isEnabled 是否可选
 * @param reserveHour 接口用时段，如 "18:00-19:00"，用于预约提交
 */
data class TimeSlotUi(
    val label: String,
    val isEnabled: Boolean = true,
    val reserveHour: String = label.replace("~", "-")
)

/**
 * 日程页「选择时间」模块。
 * 「选择时间」标题下方为时间槽芯片列表，随 [selectedDate] 变化；选中态深底白字，未选浅底深字，禁用浅底灰字。
 *
 * @param selectedDate 当前选中的日期，用于驱动时间槽数据
 * @param timeSlots 当前展示的时间槽（建议由上层根据 selectedDate 计算）
 * @param selectedSlotIndex 当前选中的时间槽下标，null 表示未选
 * @param onSlotClick 点击时间槽回调（下标）
 */
@Composable
fun ScheduleListSection(
    selectedDate: TeachingDate?,
    timeSlots: List<TimeSlotUi>,
    selectedSlotIndex: Int?,
    onSlotClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "选择时间",
            style = FontSemiBold(fontSize = 16, color = Colors.black_101828),
            modifier = Modifier.padding(vertical = 12.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 按行展示芯片，每行 3 个，行间距 12dp、芯片间距 10dp（Figma 283-30279）
            timeSlots.chunked(3).forEach { rowSlots ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowSlots.forEachIndexed { _, slot ->
                        val globalIndex = timeSlots.indexOf(slot)
                        TimeSlotChip(
                            label = slot.label,
                            isSelected = globalIndex == selectedSlotIndex,
                            isEnabled = slot.isEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (slot.isEnabled) onSlotClick(globalIndex)
                            }
                        )
                    }
                    repeat(3 - rowSlots.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * 单颗时间槽芯片：8dp 圆角，16dp 横 11dp 纵内边距；选中 #101828+白字，未选 #F8F9FA+深字，禁用 #F8F9FA+灰字。
 */
@Composable
private fun TimeSlotChip(
    label: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val backgroundColor = if (isSelected) Colors.black_101828 else Colors.gray_F8F9FA
    val textColor: Color = when {
        isSelected -> Color.White
        isEnabled -> Colors.black_101828
        else -> Colors.gray_B1B8C6
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = FontRegular(fontSize = 14, color = textColor),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
@androidx.compose.ui.tooling.preview.Preview
private fun ScheduleListSectionPreview() {
    ScheduleListSection(
        selectedDate = TeachingDate.today(),
        timeSlots = listOf(
            TimeSlotUi("18:00~19:00", true),
            TimeSlotUi("19:00~20:00", true),
            TimeSlotUi("20:00~21:00", true),
            TimeSlotUi("21:00~22:00", true),
            TimeSlotUi("22:00~23:00", false)
        ),
        selectedSlotIndex = 0,
        onSlotClick = {}
    )
}
