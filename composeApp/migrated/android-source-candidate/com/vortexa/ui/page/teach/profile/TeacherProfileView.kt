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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.page.profile.other.OtherUserProfileActivity
import com.vortexa.ui.theme.BaseTheme
import kotlin.math.abs
import vortexa.composeapp.generated.resources.Res

/**
 * 导师个人主页（Figma 283-30351 + 283-30356）。
 * 根据 [teacherId] 请求 /v/api/c2c/teacher/detail，用 [PageStatusView] 展示加载/失败状态。
 */
@Composable
fun TeacherProfileView(
    teacherId: Long,
    onBackClick: () -> Unit = {},
) {
    val viewModel = viewModel<TeacherProfileViewModel>()
    val pageStatus by viewModel.pageStatus.collectAsState()
    val baseInfo by viewModel.baseInfo.collectAsState()
    val reviews by viewModel.reviewList.collectAsState(initial = emptyList())
    val courses by viewModel.courseList.collectAsState(initial = emptyList())
    val context = LocalContext.current

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
                                OtherUserProfileActivity.start(context, userId)
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

                TeacherProfileBottomButtons()
            }
//            PageStatusView(
//                status = pageStatus,
//                modifier = Modifier.fillMaxSize(),
//                onRefresh = { viewModel.refreshDetail() }
//            )
        }
    }
}

/** 报价展示为「XX积分/小时」；整数不显示小数，非整数去掉末尾无意义的 0。 */
private fun formatTeacherPricePointsPerHour(price: Float): String {
    val intPart = price.toInt()
    val text = if (abs(price - intPart) < 1e-4f) {
        intPart.toString()
    } else {
        String.format("%.2f", price).trimEnd('0').trimEnd('.')
    }
    return "${text}积分/小时"
}

@Composable
@Preview
fun TeacherProfilePreview() {
    TeacherProfileView(teacherId = 66666)
}
