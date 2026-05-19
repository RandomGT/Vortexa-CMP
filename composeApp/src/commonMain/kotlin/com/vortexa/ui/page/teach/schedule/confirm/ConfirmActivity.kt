package com.vortexa.ui.page.teach.schedule.confirm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.vortexa.ui.page.teach.schedule.confirm2.Confirm2ViewModel
import com.vortexa.ui.theme.BaseTheme
import com.vortexa.ui.viewmodel.vortexaViewModel

object ConfirmActivity {
    const val EXTRA_TEACHER_ID: String = "teacher_id"
    const val EXTRA_RESERVE_DATE: String = "reserve_date"
    const val EXTRA_RESERVE_HOUR: String = "reserve_hour"

    fun start(context: Any?, teacherId: Long, reserveDate: String, reserveHour: String) {}
}

@Composable
fun ConfirmRoute(
    teacherId: Long,
    reserveDate: String,
    reserveHour: String,
    teacherNameFallback: String = "",
    onBackClick: () -> Unit = {},
    onModifyClick: () -> Unit = onBackClick,
    onConfirmClick: (teacherId: Long, reserveDate: String, reserveHour: String) -> Unit = { _, _, _ -> },
    viewModel: Confirm2ViewModel = vortexaViewModel(
        key = "order-confirm-$teacherId-$reserveDate-$reserveHour"
    ) { Confirm2ViewModel(teacherId, reserveDate, reserveHour) }
) {
    val teacherName by viewModel.teacherDisplayName.collectAsState()
    val teacherAvatarUrl by viewModel.teacherAvatarUrl.collectAsState()
    val guideFee by viewModel.guideFeeText.collectAsState()
    val totalPoints by viewModel.totalPointsText.collectAsState()
    val displayName = teacherName.ifBlank { teacherNameFallback.ifBlank { "…" } }

    BaseTheme(belowStatusBar = true, aboveNavigationBar = true) {
        ConfirmView(
            reserveDate = reserveDate,
            reserveHour = reserveHour,
            teacherName = displayName,
            teacherAvatarUrl = teacherAvatarUrl,
            orderPriceText = guideFee,
            payAmountText = totalPoints,
            onBackClick = onBackClick,
            onModifyClick = onModifyClick,
            onConfirmClick = { onConfirmClick(teacherId, reserveDate, reserveHour) }
        )
    }
}
