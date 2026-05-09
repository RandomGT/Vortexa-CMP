package com.vortexa.ui.page.teach.video

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.component.AvatarImage
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontSemiBold

/**
 * 用户列表项（Figma 283-31066）：头像 + 导师/学员标签 + 姓名。
 *
 * @param name 显示名称
 * @param isTutor true 为导师（蓝框+蓝标签），false 为学员（灰框+深色标签）
 * @param avatarUrl 头像 URL，为 null 时显示默认头像占位
 */
@Composable
fun VideoRtcUserItem(
    name: String,
    isTutor: Boolean,
    avatarUrl: String? = null,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isTutor) Colors.blue_74B9FD else Color.White.copy(alpha = 0.2f)
    val tagBg = if (isTutor) Colors.blue_277DFF else Colors.black_101828

    Column(
        modifier = modifier.width(84.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .border(
                    width = 1.125.dp,
                    color = borderColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            AvatarImage(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(Colors.black_101828),
                avatarUrl = avatarUrl,
                contentDescription = name
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(tagBg, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isTutor) "导师" else "学员",
                    style = FontMedium(fontSize = 12, color = Color.White)
                )
            }
        }
        Text(
            text = name,
            style = FontSemiBold(fontSize = 15, color = Color.White)
        )
    }
}

@Composable
@Preview(backgroundColor = 0xFF101828)
private fun VideoRtcUserItemPreview() {
    VideoRtcUserItem(name = "Mepo", isTutor = true)
}
