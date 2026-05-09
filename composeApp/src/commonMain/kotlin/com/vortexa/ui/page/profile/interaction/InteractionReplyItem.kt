package com.vortexa.ui.page.profile.interaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vortexa.ui.component.AvatarImage
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.profile_default

/**
 * 互动管理 - 回复列表项（Figma 504-50421）。
 * 左侧头像（使用 userAvatar），右侧昵称、时间、回复内容（回复 xxx: 正文）。
 *
 * @param nickname 用户昵称
 * @param time 时间文案
 * @param replyTo 被回复者昵称
 * @param content 回复正文
 * @param userAvatar 用户头像 URL，为空时显示昵称首字占位
 * @param onClick 整条点击回调，可选
 */
@Composable
fun InteractionReplyItem(
    nickname: String,
    time: String,
    replyTo: String,
    content: String,
    userAvatar: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null,
                    onClick = onClick
                ) else Modifier
            ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 头像 40dp：有 userAvatar 则加载网络图，否则昵称首字占位
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Colors.gray_F3F5F7),
            contentAlignment = Alignment.Center
        ) {
            val avatarUrl = userAvatar?.takeIf { it.isNotBlank() }
            if (avatarUrl != null) {
                AvatarImage(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    avatarUrl = avatarUrl,
                    contentDescription = nickname,
                    defaultResId = Res.drawable.profile_default
                )
            } else {
                Text(
                    text = nickname.firstOrNull()?.toString() ?: "?",
                    style = FontMedium(fontSize = 16, color = Colors.gray_6A7282)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nickname,
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
                text = "回复 $replyTo: $content",
                style = FontRegular(fontSize = 13, color = Colors.gray_6A7282),
                maxLines = 8,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun InteractionReplyItemPreview() {
    InteractionReplyItem(
        nickname = "用户昵称",
        time = "2 分钟前",
        replyTo = "张三",
        content = "这条回复的内容预览，超出省略。"
    )
}
