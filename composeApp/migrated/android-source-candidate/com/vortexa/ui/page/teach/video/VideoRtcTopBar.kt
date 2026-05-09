package com.vortexa.ui.page.teach.video

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.FontMedium
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res

/**
 * 1对1 视频直播页顶部栏（Figma 283-31060）：左侧菜单图标，右侧计时文案 + 秒表图标。
 *
 * @param durationText 显示时长文案，如 "01:35:44"
 * @param onMenuClick 点击菜单回调
 */
@Composable
fun VideoRtcTopBar(
    durationText: String,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = durationText,
                style = FontMedium(fontSize = 16, color = Color.White),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                painter = painterResource(Res.drawable.icon_watch),
                contentDescription = "计时",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
@Preview(backgroundColor = 0xFF101828)
private fun VideoRtcTopBarPreview() {
    VideoRtcTopBar(durationText = "01:35:44", onMenuClick = {})
}
