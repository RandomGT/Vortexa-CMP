package com.vortexa.ui.page.post.detail.reply

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.BaseTheme
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click

/**
 * 回复指示条，在键盘弹起时显示在底部输入栏上方。
 * 单行显示：回复：头像 作者名 正文内容... [X]
 *
 * @param target 回复目标信息
 * @param onClose 关闭回复指示条
 */
@Composable
fun ReplyIndicatorBar(
    target: ReplyTarget,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Colors.gray_F3F5F7)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "回复：",
            style = FontRegular(fontSize = 12, color = Colors.gray_6A7282),
            maxLines = 1
        )

        // 头像占位
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(Colors.gray_E5E8EB),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = target.authorName.firstOrNull()?.toString() ?: "U",
                style = FontMedium(fontSize = 10, color = Colors.gray_6A7282)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = target.authorName,
            style = FontMedium(fontSize = 12, color = Colors.black_101828),
            maxLines = 1
        )
        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = target.content,
            style = FontRegular(fontSize = 12, color = Colors.gray_6A7282),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "关闭回复",
            modifier = Modifier
                .size(16.dp)
                .click { onClose() },
            tint = Colors.gray_6A7282
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReplyIndicatorBarPreview() {
    BaseTheme {
        ReplyIndicatorBar(
            target = ReplyTarget(
                id = "c1",
                authorName = "张三",
                content = "感谢分享！最近也在关注合约，请问你一般用多少倍杠杆？这个问题我一直在研究",
                isComment = true
            ),
            onClose = {}
        )
    }
}
