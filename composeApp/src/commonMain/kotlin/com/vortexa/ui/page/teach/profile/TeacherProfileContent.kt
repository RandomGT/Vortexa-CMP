package com.vortexa.ui.page.teach.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.ui.theme.FontSemiBold
import com.vortexa.ui.component.AvatarImage
import com.vortexa.ui.component.StarRating
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.ic_arrow_right_gray

/**
 * 教师资料内容区（Figma 283-30356）：头像、姓名、星级、咨询/报价、简介、课程著作标签。
 *
 * @param modifier 外层布局修饰符
 * @param avatarUrl 头像网络地址，空或加载失败时使用 [AvatarImage] 默认头像（`Res.drawable.profile_default`）
 * @param name 展示姓名
 * @param starCount 星级评分 0f..5f
 * @param consultationCount 咨询次数
 * @param price 报价展示文案
 * @param intro 简介
 * @param courseTitles 课程著作标签文案列表
 * @param onCourseClick 点击某一课程标签
 */
@Composable
fun TeacherProfileContent(
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    name: String = "Capper",
    starCount: Float = 4f,
    consultationCount: Int = 1123,
    price: String = "60积分/小时",
    intro: String = "简介：这是一个很厉害的老师这是一个很厉害的老师这是一个很厉害的老师",
    courseTitles: List<String> = emptyList(),
    onCourseClick: (String) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 18.dp)
            .padding(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 头像 + 右侧信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarImage(
                avatarUrl = avatarUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = name,
                    style = FontSemiBold(
                        fontSize = 20,
                        color = androidx.compose.ui.graphics.Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                StarRating(litCount = starCount)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "咨询次数: $consultationCount",
                        style = FontRegular(fontSize = 16, color = Colors.gray_6A7282),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "报价: $price",
                        style = FontMedium(fontSize = 16, color = Colors.gray_B1B8C6),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Text(
            text = intro,
            style = FontRegular(fontSize = 16, color = Colors.gray_6A7282),
            modifier = Modifier.fillMaxWidth()
        )
        if (courseTitles.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "课程著作：",
                    style = FontRegular(fontSize = 16, color = Colors.gray_6A7282)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    courseTitles.forEach { title ->
                        CourseChip(
                            title = title,
                            onClick = { onCourseClick(title) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 课程标签：深色圆角背景 + 金色文字 + 右箭头。
 */
@Composable
private fun CourseChip(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(
                color = Colors.chipBg_3C3B36,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 7.dp, vertical = 2.5.dp)
            .click(onClickListener = onClick),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = FontRegular(fontSize = 12, color = Colors.gold_F6BD49)
        )
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_right_gray),
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = Colors.gold_F6BD49
        )
    }
}
