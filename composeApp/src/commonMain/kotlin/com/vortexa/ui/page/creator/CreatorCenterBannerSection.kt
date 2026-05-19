package com.vortexa.ui.page.creator

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vortexa.model.CreatorActivity
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular

@Composable
fun CreatorCenterBannerSection(
    activities: List<CreatorActivity> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier.horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (activities.isEmpty()) {
            CreatorCenterBannerItem(title = "暂无活动")
        } else {
            activities.forEach { activity ->
                CreatorCenterBannerItem(title = activity.title)
            }
        }
    }
}

@Composable
private fun CreatorCenterBannerItem(
    title: String = "有奖活动",
) {
    Box(
        modifier = Modifier
            .size(width = 227.dp, height = 128.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFEDF8FF), Colors.blue_E0F3FF),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color(0xFF1B54FF), RoundedCornerShape(bottomEnd = 8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                text = "有奖活动",
                style = FontRegular(fontSize = 12, color = Color.White),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = title,
                style = FontRegular(fontSize = 14, color = Colors.black_101828),
                maxLines = 2,
            )
        }
    }
}
