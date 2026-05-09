package com.vortexa.ui.page.home.pager.home.communicate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vortexa.model.CourseRecommendItem
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import java.util.LinkedList
import vortexa.composeapp.generated.resources.Res

/** 封面占位背景色（Figma bg_card #EEF0F1） */
private val CoverBgColor = Color(0xFFEEF0F1)

/**
 * 单个课程推荐项（Figma 747-82974）：封面、标题+右箭头、讲师头像+姓名、人在学文案。
 *
 * @param item 课程数据
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
private fun CourseRecommendItem(
    item: CourseRecommendItem,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val annotatedStudentCount = buildAnnotatedString {
        val suffix = "人在学"
        val text = item.studentCountText
        val highlightEnd = text.indexOf(suffix)
        if (highlightEnd > 0) {
            withStyle(SpanStyle(color = Colors.red_FF383C)) {
                append(text.substring(0, highlightEnd))
            }
            withStyle(SpanStyle(color = Colors.gray_6A7282)) {
                append(suffix)
            }
        } else {
            append(text)
        }
    }

    Row(
        modifier = modifier
            .padding(end = 32.dp)
            .width(320.dp)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 封面 80x60，圆角 5dp
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(CoverBgColor)
        )
        // 信息区
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 标题 + chevron
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    style = FontMedium(fontSize = 16, color = Colors.black_101828),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_right_gray),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Colors.gray_6A7282
                )
            }
            // 讲师头像 + 姓名 | 人在学
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Colors.black_101828),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.lecturerName.take(1),
                            style = FontRegular(fontSize = 10, color = Color.White)
                        )
                    }
                    Text(
                        text = item.lecturerName,
                        style = FontRegular(fontSize = 12, color = Colors.black_101828),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = annotatedStudentCount,
                    style = FontRegular(fontSize = 12, color = Colors.gray_6A7282)
                )
            }
        }
    }
}

/**
 * 课程推荐横向滚动区域（Figma 747-82974）：最多展示 6 条课程，支持横向滚动。
 *
 * @param items 课程列表，最多取 6 条
 * @param onItemClick 课程项点击回调
 * @param modifier 修饰符
 */
@Composable
fun CourseRecommendHorizontal(
    items: List<CourseRecommendItem>,
    onItemClick: (CourseRecommendItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val displayList = items.take(6)
    val pairList = LinkedList<Pair<CourseRecommendItem, CourseRecommendItem?>>()
    (0..displayList.size / 2).forEach {
        pairList.push(Pair(displayList[it], displayList.getOrNull(it + 1)))
    }
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp)
    ) {
        itemsIndexed(pairList) { index, item ->
            Column {
                CourseRecommendItem(
                    item = item.first,
                    onClick = { onItemClick(item.first) }
                )
                if (item.second != null) {
                    CourseRecommendItem(
                        item = item.second!!,
                        onClick = { onItemClick(item.second!!) }
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun CourseRecommendHorizontalPreview() {
    CourseRecommendHorizontal(
        items = listOf(
            CourseRecommendItem("c1", "课程 A", "讲师甲", "100+人在学"),
            CourseRecommendItem("c2", "课程 B", "讲师乙", "200+人在学")
        )
    )
}
