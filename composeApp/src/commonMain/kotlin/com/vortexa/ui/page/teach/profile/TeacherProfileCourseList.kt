package com.vortexa.ui.page.teach.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.model.SchoolCourseCard
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.page.home.pager.school.VortexaSchoolCardItem
import kotlin.collections.chunked

/**
 * 教师资料页「课程展示」Tab 的列表：两列网格，每项使用 [VortexaSchoolCardItem]。
 * 数据来自 [TeacherProfileViewModel.courseList]；列表为空时展示通用空页面 [PageStatusView]。
 *
 * @param courses 课程卡片列表
 */
@Composable
fun TeacherProfileCourseList(
    courses: List<SchoolCourseCard>,
    modifier: Modifier = Modifier
) {
    if (courses.isEmpty()) {
        PageStatusView(
            status = PageStatus.Empty,
            modifier = modifier.fillMaxSize()
        )
        return
    }

    val horizontalPadding = 18.dp
    val itemSpacing = 14.dp
    val verticalSpacing = 24.dp
    val rows = courses.chunked(2)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        itemsIndexed(
            rows,
            key = { index, _ -> index }
        ) { index, rowCards ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        top = if (index == 0) 16.dp else verticalSpacing
                    ),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {
                rowCards.forEach { card ->
                    VortexaSchoolCardItem(
                        card = card,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowCards.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Preview
@Composable
private fun TeacherProfileCourseListPreview() {
    TeacherProfileCourseList(
        courses = listOf(
            SchoolCourseCard(
                id = "p1",
                title = "量化交易一阶课程",
                teacherName = "幻夜星辰大海",
                purchaseCount = "200次",
                tags = listOf("量化交易", "短线"),
                price = 36.5f,
                unit = "USD",
                rating = 4.5f
            )
        )
    )
}
