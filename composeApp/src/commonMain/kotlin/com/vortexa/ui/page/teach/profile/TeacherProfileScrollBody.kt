package com.vortexa.ui.page.teach.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.model.SchoolCourseCard
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.page.home.pager.school.VortexaSchoolCardItem
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontSemiBold
import kotlin.collections.chunked

/**
 * 学员评价与课程展示纵向列表（原 Tab 合并为一屏滚动）。
 */
@Composable
fun TeacherProfileScrollBody(
    reviews: List<TeacherReviewItem>,
    courses: List<SchoolCourseCard>,
    modifier: Modifier = Modifier,
) {
    val courseRows = remember(courses) { courses.chunked(2) }
    val horizontalPadding = 18.dp
    val itemSpacing = 14.dp
    val verticalSpacing = 24.dp

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        item {
            Text(
                text = "学员评价",
                style = FontSemiBold(16, Colors.black_101828),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = horizontalPadding, end = horizontalPadding, top = 16.dp, bottom = 8.dp)
            )
        }
        if (reviews.isEmpty()) {
            item {
                PageStatusView(
                    status = PageStatus.Empty,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        } else {
            items(reviews, key = { it.id }) { item ->
                TeacherProfileReviewItem(item = item)
            }
        }

        item {
            Text(
                text = "课程展示",
                style = FontSemiBold(16, Colors.black_101828),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = horizontalPadding, end = horizontalPadding, top = 8.dp, bottom = 8.dp)
            )
        }
        if (courses.isEmpty()) {
            item {
                PageStatusView(
                    status = PageStatus.Empty,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        } else {
            itemsIndexed(
                courseRows,
                key = { index, _ -> "course_row_$index" }
            ) { index, rowCards ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = horizontalPadding,
                            end = horizontalPadding,
                            top = if (index == 0) 8.dp else verticalSpacing
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
}

@Preview
@Composable
private fun TeacherProfileScrollBodyPreview() {
    TeacherProfileScrollBody(
        reviews = listOf(
            TeacherReviewItem("1", "Leo", "18:33", 3.5f, "太牛逼了", "太牛逼了")
        ),
        courses = emptyList()
    )
}
