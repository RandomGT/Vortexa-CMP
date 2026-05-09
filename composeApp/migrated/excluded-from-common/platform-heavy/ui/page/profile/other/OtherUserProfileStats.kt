package com.vortexa.ui.page.profile.other

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular

/**
 * 他人主页关注 / 粉丝统计（Figma 415-41955，对齐「我的」页统计排布）。
 */
@Composable
fun OtherUserProfileStats(
    followCount: Int,
    fanCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .height(48.dp)
            .background(Colors.gray_EEF0F1, RoundedCornerShape(6.dp)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = followCount.toString(),
                style = FontMedium(fontSize = 18, color = Colors.black_242424),
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "关注",
                style = FontRegular(fontSize = 14, color = Colors.gray_6A7282),
                lineHeight = 20.sp
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = fanCount.toString(),
                style = FontMedium(fontSize = 18, color = Colors.black_242424),
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "粉丝",
                style = FontRegular(fontSize = 14, color = Colors.gray_6A7282),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun OtherUserProfileStatsPreview() {
    OtherUserProfileStats(followCount = 120, fanCount = 3408)
}
