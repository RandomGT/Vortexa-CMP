package com.vortexa.ui.page.teach.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.component.LoadingButton
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.page.teach.helper.TeachingDate
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.ui.theme.Colors

/**
 * 日程页：上为日历模块，下为「选择时间」时间槽列表。
 * 时间槽由 [viewModel] 根据 [teacherId] 与选中日期请求 /v/api/c2c/teacher/reserve/time。
 *
 * @param teacherId 导师 ID，由启动页（如 [ScheduleActivity]）传入
 */
@Composable
fun ScheduleView(
    teacherId: Long,
    onPayConfirmClick: (teacherId: Long, reserveDate: String, reserveHour: String) -> Unit = { _, _, _ -> },
    viewModel: ScheduleViewModel = vortexaViewModel(key = "schedule-$teacherId") { ScheduleViewModel(teacherId) }
) {
    var selectedDate by remember { mutableStateOf<TeachingDate?>(TeachingDate.today()) }
    var selectedSlotIndex by remember { mutableIntStateOf(-1) }
    val timeSlots by viewModel.timeSlots.collectAsState()
    val pageStatus by viewModel.pageStatus.collectAsState()
    val reserveLoading by viewModel.reserveLoading.collectAsState()

    LaunchedEffect(selectedDate) {
        viewModel.loadReserveTime(selectedDate)
        selectedSlotIndex = -1
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        ScheduleCalendarSection(
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it }
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            ScheduleListSection(
                modifier = Modifier.fillMaxSize(),
                selectedDate = selectedDate,
                timeSlots = timeSlots,
                selectedSlotIndex = selectedSlotIndex,
                onSlotClick = { index ->
                    selectedSlotIndex = if (selectedSlotIndex == index) -1 else index
                }
            )
            PageStatusView(
                status = pageStatus,
                modifier = Modifier.fillMaxSize(),
                onRefresh = { viewModel.loadReserveTime(selectedDate) }
            )
        }

        LoadingButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .padding(horizontal = 18.dp)
                .height(51.dp)
                .background(Colors.black_101828, RoundedCornerShape(30.dp)),
            text = "预约&支付",
            isLoading = reserveLoading,
            onClick = {
                val date = selectedDate ?: return@LoadingButton
                if (selectedSlotIndex < 0 || selectedSlotIndex >= timeSlots.size) return@LoadingButton
                val slot = timeSlots[selectedSlotIndex]
                if (!slot.isEnabled) return@LoadingButton
                onPayConfirmClick(teacherId, date.formatSlash(), slot.reserveHour)
            }
        )
    }
}

@Composable
@Preview
fun SchedulePreview() {
    ScheduleView(teacherId = 1L)
}
