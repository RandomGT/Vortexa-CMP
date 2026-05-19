package com.vortexa.ui.page.creator.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vortexa.model.CreatorStatisticsPostItem
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.eye
import vortexa.composeapp.generated.resources.heart_small
import vortexa.composeapp.generated.resources.message_circle

@Composable
fun DataCenterPostItem(
    item: CreatorStatisticsPostItem,
    onItemClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val clickModifier = if (onItemClick != null) Modifier.clickable(onClick = onItemClick) else Modifier

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(clickModifier)
            .padding(vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Colors.gray_F3F5F7),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item.nickname.firstOrNull()?.toString() ?: "U",
                    style = FontMedium(fontSize = 14, color = Colors.gray_6A7282),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nickname,
                    style = FontMedium(fontSize = 14, color = Colors.black_101828),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.publishTime ?: "",
                    style = FontRegular(fontSize = 11, color = Colors.gray_6A7282),
                )
            }
        }

        Text(
            text = item.title ?: "",
            style = FontMedium(fontSize = 16, color = Colors.black_101828),
            modifier = Modifier.padding(top = 10.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = item.summary ?: "",
            style = FontRegular(fontSize = 15, color = Colors.gray_6A7282),
            modifier = Modifier.padding(top = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatItem(Res.drawable.eye, formatCount(item.viewCount))
            StatItem(Res.drawable.heart_small, formatCount(item.likeCount.toLong()))
            StatItem(Res.drawable.message_circle, formatCount(item.replyCount.toLong()))
        }
    }
}

@Composable
private fun StatItem(iconRes: DrawableResource, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = Colors.gray_6A7282,
        )
        Text(
            text = value,
            style = FontRegular(fontSize = 14, color = Colors.gray_6A7282),
        )
    }
}

private fun formatCount(count: Long): String = when {
    count >= 1_000_000 -> "${count / 1_000_000}M"
    count >= 1_000 -> "${count / 1_000}K"
    else -> count.toString()
}
