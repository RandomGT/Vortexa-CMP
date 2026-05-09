package com.vortexa.ui.page.teach.myclass.school

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.model.SchoolCourseCard
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular

/** 课程卡片圆角：Figma 788-73189 上 30dp、下 16dp */
private val cardShape = RoundedCornerShape(
    topStart = 30.dp,
    topEnd = 30.dp,
    bottomEnd = 16.dp,
    bottomStart = 16.dp
)

/** 封面区背景色 Figma bg_card #EEF0F1 */
private val coverBg = Color(0xFFEEF0F1)

/** 下架按钮背景 Figma rgba(224,46,42,0.12) */
private val offShelfBg = Color(0x1FE02E2A)

/** 下架按钮文字色 Figma #e02e2a */
private val offShelfText = Color(0xFFE02E2A)

/**
 * 涡联学院课程单卡（Figma 788-73189）
 * 含封面区、标题、已上架/购买人数、标签、底部「下架」按钮。
 *
 * @param card 课程卡片数据
 * @param onOffShelfClick 点击下架按钮回调
 */
@Composable
fun MyClassSchoolCardItem(
    card: SchoolCourseCard,
    onOffShelfClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = cardShape,
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(0.dp)) {
            // 封面区：170:127 比例，8dp 圆角
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(127.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(coverBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VORTEXA",
                    style = FontRegular(fontSize = 10, color = Colors.gray_B1B8C6)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = card.title,
                    style = FontMedium(fontSize = 14, color = Colors.black_101828),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "已上架",
                        style = FontRegular(fontSize = 12, color = Colors.gray_6A7282)
                    )
                    Text(
                        text = "${card.purchaseCount}人已购",
                        style = FontRegular(fontSize = 12, color = Colors.blue_277DFF)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    card.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x146A7282)
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                style = FontRegular(fontSize = 12, color = Colors.gray_6A7282)
                            )
                        }
                    }
                }
                // 下架按钮：Figma 评论框样式，圆角 24dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(offShelfBg)
                        .clickable(onClick = onOffShelfClick)
                        .padding(vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "下架",
                        style = FontMedium(fontSize = 12, color = offShelfText)
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun MyClassSchoolCardItemPreview() {
    MyClassSchoolCardItem(
        card = SchoolCourseCard(
            id = "1",
            title = "量化交易一阶课程",
            teacherName = "",
            purchaseCount = "30",
            tags = listOf("量化交易", "短线"),
            price = 0f,
            unit = "",
            rating = 0f
        )
    )
}
