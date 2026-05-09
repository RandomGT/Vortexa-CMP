package com.vortexa.ui.page.home.pager.message

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.icon_bell

/**
 * 单条消息列表项：左侧通知图标（浅蓝圆+铃铛+未读角标），右侧标题、时间、内容预览。
 *
 * @param title 标题，如「系统通知」
 * @param time 时间文案，如「2025/12/01 11:00」
 * @param preview 内容预览，超出省略
 * @param unreadCount 未读数量，0 不显示角标
 * @param onClick 点击整条回调
 */
@Composable
fun MessageListItem(
    title: String,
    time: String,
    preview: String,
    unreadCount: Int = 0,
    onClick: () -> Unit
) {
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 左侧：浅蓝圆 + 铃铛图标 + 红色角标
            Box(
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Colors.blue_E0F3FF),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.icon_bell),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }

                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-4).dp)
                            .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                            .height(16.dp)
                            .background(Colors.red_FF383C, CircleShape)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            style = FontMedium(fontSize = 9, color = androidx.compose.ui.graphics.Color.White).copy(
                                lineHeight = 9.sp,
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 右侧：标题、时间、内容预览
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = FontMedium(fontSize = 15, color = Colors.black_101828),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = time,
                        style = FontRegular(fontSize = 12, color = Colors.gray_B1B8C6)
                    )
                }
                Text(
                    text = preview,
                    style = FontRegular(fontSize = 13, color = Colors.gray_6A7282),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

    }
}

@Composable
fun MessageListItemPreview() {
    MessageListItem("123", "10:33", "123", 6) {

    }
}
