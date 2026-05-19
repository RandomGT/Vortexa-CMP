package com.vortexa.ui.page.creator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular

@Composable
fun CreatorCenterDataCard(
    days: Int = 7,
    postCount: Int = 1,
    viewCount: Int = 250,
    likeCount: Int = 20,
    commentCount: Int = 33,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Colors.gray_F8F9FA)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = "近${days}日数据",
            style = FontMedium(fontSize = 14, color = Colors.black_101828),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
        ) {
            DataItem(Modifier.weight(1f), "发帖数", postCount.toString())
            DataItem(Modifier.weight(1f), "内容浏览", viewCount.toString())
            DataItem(Modifier.weight(1f), "点赞", likeCount.toString())
            DataItem(Modifier.weight(1f), "评论", commentCount.toString())
        }
    }
}

@Composable
private fun DataItem(modifier: Modifier, label: String, value: String) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = FontRegular(fontSize = 12, color = Colors.gray_6A7282),
        )
        Text(
            text = value,
            style = FontRegular(fontSize = 14, color = Colors.black_101828),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
