package com.vortexa.ui.page.teach.schedule.confirm

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import com.vortexa.ui.component.AvatarImage

/**
 * 订单/支付确认页卡片：文案固定「一对一指导」+ 导师头像与昵称（数据来自预约导师，无点击跳转）。
 *
 * @param teacherName 导师昵称
 * @param teacherAvatarUrl 导师头像 URL，null 时显示占位
 */
@Composable
fun ConfirmCourseCard(
    teacherName: String,
    teacherAvatarUrl: String? = null,
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
                text = "一对一指导",
                style = FontMedium(fontSize = 16, color = Colors.black_101828)
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarImage(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape),
                    avatarUrl = teacherAvatarUrl,
                    contentDescription = null
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
private fun ConfirmCourseCardPreview() {
    ConfirmCourseCard(
        teacherName = "刘宇凡",
        teacherAvatarUrl = null
    )
}
