package com.vortexa.ui.page.creator.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.model.CreatorStatisticsPostItem
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import vortexa.composeapp.generated.resources.Res

/**
 * 数据中心帖子列表项（Figma 504-51187）：头像、昵称、时间、标题、一行摘要、互动统计。
 * 与 PostItem 类似，但详情缩略为一行展示。
 *
 * @param item 帖子统计数据
 * @param onItemClick 点击进入帖子详情
 */
@Composable
fun DataCenterPostItem(
    item: CreatorStatisticsPostItem,
    onItemClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clickModifier = if (onItemClick != null) {
        Modifier.clickable(onClick = onItemClick)
    } else Modifier

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(clickModifier)
            .padding(vertical = 10.dp)
    ) {
        // 头像 + 昵称 + 时间
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Colors.gray_F3F5F7),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.nickname.firstOrNull()?.toString() ?: "U",
                    style = FontMedium(fontSize = 14, color = Colors.gray_6A7282)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nickname,
                    style = FontMedium(fontSize = 14, color = Colors.black_101828),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.publishTime ?: "",
                    style = FontRegular(fontSize = 11, color = Colors.gray_6A7282)
                )
            }
        }

        // 标题
        Text(
            text = item.title ?: "",
            style = FontMedium(fontSize = 16, color = Colors.black_101828),
            modifier = Modifier.padding(top = 10.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // 一行摘要
        Text(
            text = item.summary ?: "",
            style = FontRegular(fontSize = 15, color = Colors.gray_6A7282),
            modifier = Modifier.padding(top = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // 互动统计：浏览、点赞、评论
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(Res.drawable.eye, formatCount(item.viewCount))
            StatItem(Res.drawable.heart_small, formatCount(item.likeCount.toLong()))
            StatItem(Res.drawable.message_circle, formatCount(item.replyCount.toLong()))
        }
    }
}

@Composable
private fun StatItem(iconRes: Int, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = Colors.gray_6A7282
        )
        Text(
            text = value,
            style = FontRegular(fontSize = 14, color = Colors.gray_6A7282)
        )
    }
}

private fun formatCount(count: Long): String = when {
    count >= 1_000_000 -> "${count / 1_000_000}M"
    count >= 1_000 -> "${count / 1_000}K"
    else -> count.toString()
}

@Composable
@Preview
private fun DataCenterPostItemPreview() {
    val mockItem = CreatorStatisticsPostItem(
        postId = 1,
        nickname = "Kaelani Silvermoon",
        avatar = null,
        publishTime = "2025-09-15  09:00:23",
        title = "关于比特币：难忘的瞬间",
        summary = "关于比特币、区块链和加密货币趋势的最新见解。",
        viewCount = 163000,
        likeCount = 163000,
        replyCount = 12000,
        shareCount = 12000,
        revenue = 12000
    )
    DataCenterPostItem(item = mockItem)
}
