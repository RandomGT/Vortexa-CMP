package com.vortexa.ui.page.teach.schedule.confirm2

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
import com.vortexa.ui.page.teach.schedule.confirm.ConfirmCourseCard
import com.vortexa.ui.theme.belowStatusBar

/**
 * 支付确认页主视图（Figma 422-36569）：TopBar + 课程卡片 + 预约详情 + 底部「返回」「确认支付」；确认支付为 LoadingButton，请求中显示 loading。一期不展示优惠券。
 */
@Composable
fun PayConfirmView(
    teacherName: String,
    teacherAvatarUrl: String?,
    /** 课程开始时间展示，由上一页传入的预约日期与时段拼接 */
    courseStartTime: String,
    durationText: String,
    guideFee: String,
    balancePoints: String,
    totalPoints: String,
    payLoading: Boolean,
    onBackClick: () -> Unit,
    onPayClick: () -> Unit
) {
    val detailRows = listOf(
        "课程开始时间" to courseStartTime,
        "指导时长" to durationText,
        "指导费用" to guideFee,
        "积分余额" to balancePoints,
        "总计" to totalPoints
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .belowStatusBar()
            .background(Color.White)
    ) {
        PayConfirmTopBar(onBackClick = onBackClick)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            ConfirmCourseCard(
                teacherName = teacherName,
                teacherAvatarUrl = teacherAvatarUrl
            )
            PayConfirmDetailSection(rows = detailRows)
        }
        Spacer(Modifier.height(20.dp))
        PayConfirmBottomBar(
            payLoading = payLoading,
            onBackClick = onBackClick,
            onPayClick = onPayClick
        )
    }
}

@Composable
@Preview
private fun PayConfirmViewPreview() {
    PayConfirmView(
        teacherName = "刘宇凡",
        teacherAvatarUrl = null,
        courseStartTime = "2025-10-08 18:00",
        durationText = "2小时",
        guideFee = "120积分",
        balancePoints = "200积分",
        totalPoints = "120积分",
        payLoading = false,
        onBackClick = {},
        onPayClick = {}
    )
}
