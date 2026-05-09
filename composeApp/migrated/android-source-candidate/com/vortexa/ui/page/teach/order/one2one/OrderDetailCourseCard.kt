package com.vortexa.ui.page.teach.order.one2one

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.component.AvatarImage
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click

/**
 * 订单详情页课程概要：课程标题 + 可点击的导师头像与姓名（跳转导师主页）。
 */
@Composable
fun OrderDetailCourseCard(
    courseTitle: String,
    teacherName: String,
    teacherAvatarUrl: String? = null,
    onTeacherClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = courseTitle,
                style = FontMedium(fontSize = 16, color = Colors.black_101828),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .click(onTeacherClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarImage(
                    avatarUrl = teacherAvatarUrl,
                    contentDescription = "导师头像",
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Colors.gray_EEF0F1)
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    text = teacherName,
                    style = FontRegular(fontSize = 12, color = Colors.black_101828)
                )
            }
        }
    }
}

@Composable
@Preview
private fun OrderDetailCourseCardPreview() {
    OrderDetailCourseCard(
        courseTitle = "一对一指导2小时：从入门到专家精通区块链：从入门到专家",
        teacherName = "刘宇凡"
    )
}
