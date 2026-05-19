package com.vortexa.ui.page.teach.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.theme.BaseTheme
import com.vortexa.ui.viewmodel.vortexaViewModel
import kotlin.math.abs
import kotlin.math.roundToInt
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.bg_teacher_profile

/**
 * 导师个人主页（Figma 283-30351 + 283-30356）。
 * 根据 [teacherId] 请求 /v/api/c2c/teacher/detail，用 [PageStatusView] 展示加载/失败状态。
 */
@Composable
fun TeacherProfileView(
    teacherId: Long,
    onBackClick: () -> Unit = {},
    onProfileClick: (Long) -> Unit = {},
    onScheduleClick: (Long) -> Unit = {},
) {
    val viewModel = vortexaViewModel(key = "teacher-profile-$teacherId") { TeacherProfileViewModel() }
    val pageStatus by viewModel.pageStatus.collectAsState()
    val baseInfo by viewModel.baseInfo.collectAsState()
    val reviews by viewModel.reviewList.collectAsState(initial = emptyList())
    val courses by viewModel.courseList.collectAsState(initial = emptyList())

    LaunchedEffect(teacherId) {
        viewModel.loadDetail(teacherId)
    }

    BaseTheme(belowStatusBar = false, aboveNavigationBar = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(bottomEnd = 50.dp))
                ) {
                    Image(
                        painter = painterResource(Res.drawable.bg_teacher_profile),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                    Column(modifier = Modifier.fillMaxSize()) {
                        TeacherProfileHeader(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            onBackClick = onBackClick,
                            onProfileClick = {
                                val userId = baseInfo?.userId?.takeIf { it > 0L } ?: teacherId
                                onProfileClick(userId)
                            },
                        )
                        TeacherProfileContent(
                            avatarUrl = baseInfo?.avatar,
                            name = baseInfo?.nickName ?: "",
                            starCount = baseInfo?.score?.toFloatOrNull() ?: 0f,
                            consultationCount = baseInfo?.completedConsultations ?: 0,
                            price = formatTeacherPricePointsPerHour(baseInfo?.price ?: 0f),
                            intro = baseInfo?.introduction ?: "",
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
//                TeacherProfileScrollBody(
//                    reviews = reviews,
//                    courses = courses,
//                    modifier = Modifier.weight(1f)
//                )

                TeacherProfileBottomButtons(
                    onScheduleClick = {
                        onScheduleClick(baseInfo?.teacherId?.takeIf { it > 0L } ?: teacherId)
                    }
                )
            }
            PageStatusView(
                status = pageStatus,
                modifier = Modifier.fillMaxSize(),
                onRefresh = { viewModel.refreshDetail() }
            )
        }
    }
}

/** 报价展示为「XX积分/小时」；整数不显示小数，非整数去掉末尾无意义的 0。 */
private fun formatTeacherPricePointsPerHour(price: Float): String {
    val intPart = price.toInt()
    val text = if (abs(price - intPart) < 1e-4f) {
        intPart.toString()
    } else {
        val cents = (price * 100f).roundToInt().coerceAtLeast(0)
        val whole = cents / 100
        val decimal = (cents % 100).toString().padStart(2, '0')
        "$whole.$decimal".trimEnd('0').trimEnd('.')
    }
    return "${text}积分/小时"
}

@Composable
@Preview
fun TeacherProfilePreview() {
    TeacherProfileView(teacherId = 66666)
}
