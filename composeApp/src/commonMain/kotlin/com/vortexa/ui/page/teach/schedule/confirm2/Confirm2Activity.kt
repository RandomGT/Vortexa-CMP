package com.vortexa.ui.page.teach.schedule.confirm2

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.vortexa.ui.theme.BaseTheme
import com.vortexa.ui.viewmodel.vortexaViewModel

object Confirm2Activity {
    const val EXTRA_TEACHER_ID: String = "teacher_id"
    const val EXTRA_RESERVE_DATE: String = "reserve_date"
    const val EXTRA_RESERVE_HOUR: String = "reserve_hour"

    fun start(context: Any?, teacherId: Long, reserveDate: String, reserveHour: String) {}
}

@Composable
fun Confirm2Route(
    teacherId: Long,
    reserveDate: String,
    reserveHour: String,
    onBackClick: () -> Unit = {},
    onReserveSuccess: (reserveId: Long) -> Unit = {},
    viewModel: Confirm2ViewModel = vortexaViewModel(
        key = "pay-confirm-$teacherId-$reserveDate-$reserveHour"
    ) { Confirm2ViewModel(teacherId, reserveDate, reserveHour) }
) {
    val payLoading by viewModel.payLoading.collectAsState()
    val reserveSuccessReserveId by viewModel.reserveSuccessReserveId.collectAsState()
    val teacherName by viewModel.teacherDisplayName.collectAsState()
    val teacherAvatarUrl by viewModel.teacherAvatarUrl.collectAsState()
    val guideFee by viewModel.guideFeeText.collectAsState()
    val balancePoints by viewModel.balancePointsText.collectAsState()
    val totalPoints by viewModel.totalPointsText.collectAsState()
    val durationText = parseDurationFromSlot(reserveHour)
    val courseStartTime = formatCourseStartTime(reserveDate, reserveHour)

    LaunchedEffect(reserveSuccessReserveId) {
        val reserveId = reserveSuccessReserveId ?: return@LaunchedEffect
        onReserveSuccess(reserveId)
        viewModel.clearReserveSuccess()
    }

    BaseTheme(belowStatusBar = true, aboveNavigationBar = true) {
        PayConfirmView(
            teacherName = teacherName.ifBlank { "…" },
            teacherAvatarUrl = teacherAvatarUrl,
            courseStartTime = courseStartTime,
            durationText = durationText,
            guideFee = guideFee,
            balancePoints = balancePoints,
            totalPoints = totalPoints,
            payLoading = payLoading,
            onBackClick = onBackClick,
            onPayClick = { viewModel.reserve() }
        )
    }
}

private fun formatCourseStartTime(reserveDate: String, reserveHour: String): String {
    if (reserveDate.isEmpty() || reserveHour.isEmpty()) return "—"
    val start = reserveHour.substringBefore("-").trim()
    return "${reserveDate.replace("/", "-")} $start"
}

private fun parseDurationFromSlot(reserveHour: String): String {
    if (reserveHour.isEmpty()) return "2小时"
    val parts = reserveHour.split("-")
    if (parts.size != 2) return "1小时"
    val start = parts[0].trim().substringBefore(":").toIntOrNull() ?: 0
    val end = parts[1].trim().substringBefore(":").toIntOrNull() ?: 0
    val hours = (end - start).coerceAtLeast(1)
    return "${hours}小时"
}
