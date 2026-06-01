package com.vortexa.ui.page.teach.schedule.confirm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.belowStatusBar

/**
 * 订单确认页主视图（Figma 415-40747）：TopBar + 课程卡片 + 预约详情 + 底部双按钮。
 * 根据 [reserveDate]/[reserveHour] 生成预约详情行。
 *
 * @param reserveDate 预约日期，格式 yyyy/MM/dd
 * @param reserveHour 时段，如 18:00-19:00
 * @param teacherName 导师昵称（与预约导师一致）
 * @param teacherAvatarUrl 导师头像 URL
 * @param orderPriceText 「订单价格」展示文案（如 x积分）
 * @param payAmountText 「实付价格」展示文案
 * @param onBackClick 返回
 * @param onModifyClick 返回修改（返回上一页）
 * @param onConfirmClick 确认订单
 */
@Composable
fun ConfirmView(
    reserveDate: String,
    reserveHour: String,
    teacherName: String,
    teacherAvatarUrl: String?,
    orderPriceText: String,
    payAmountText: String,
    onBackClick: () -> Unit,
    onModifyClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    val durationText = parseDurationFromSlot(reserveHour)
    val orderTime = if (reserveDate.isNotEmpty()) "${reserveDate.replace("/", "-")} 16:00" else "2025-10-08 16:00"
    val courseTime = if (reserveDate.isNotEmpty() && reserveHour.isNotEmpty()) {
        val start = reserveHour.substringBefore("-").trim()
        "${reserveDate.replace("/", "-")} $start"
    } else "2025-10-08 20:00"
    val detailRows = listOf(
        "订单价格" to orderPriceText,
        "实付价格" to payAmountText,
        "下单时间" to orderTime,
        "课程时间" to courseTime,
        "课程时长" to durationText,
        "订单编号" to "16456549649612121",
        "支付方式" to "积分"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .belowStatusBar()
            .background(Color.White)
    ) {
        ConfirmTopBar(onBackClick = onBackClick)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            ConfirmCourseCard(
                teacherName = teacherName,
                teacherAvatarUrl = teacherAvatarUrl
            )
            ConfirmDetailSection(rows = detailRows)
        }
        Spacer(Modifier.height(20.dp))
        ConfirmBottomBar(
            onModifyClick = onModifyClick,
            onConfirmClick = onConfirmClick
        )
    }
}

/** 从时段字符串解析时长文案，如 "18:00-19:00" -> "1小时" */
private fun parseDurationFromSlot(reserveHour: String): String {
    if (reserveHour.isEmpty()) return "2小时"
    val parts = reserveHour.split("-")
    if (parts.size != 2) return "1小时"
    val start = parts[0].trim().substringBefore(":")
    val end = parts[1].trim().substringBefore(":")
    val startH = start.toIntOrNull() ?: 0
    val endH = end.toIntOrNull() ?: 0
    val hours = (endH - startH).coerceAtLeast(1)
    return "${hours}小时"
}

@Composable
@Preview
private fun ConfirmViewPreview() {
    ConfirmView(
        reserveDate = "2025/10/08",
        reserveHour = "18:00-19:00",
        teacherName = "刘宇凡",
        teacherAvatarUrl = null,
        orderPriceText = "60积分",
        payAmountText = "60积分",
        onBackClick = {},
        onModifyClick = {},
        onConfirmClick = {}
    )
}
