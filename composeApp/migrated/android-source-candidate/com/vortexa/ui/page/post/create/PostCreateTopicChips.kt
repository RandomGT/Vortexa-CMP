package com.vortexa.ui.page.post.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular

/**
 * 话题 Chip 行：预设话题插入完整 `#话题名`；自定义话题仅插入 `#` 由用户补全话题名（均无结尾 `#`）。
 *
 * @param topics 话题列表
 * @param onTopicClick 点击回调，参数为话题文本
 * @param onCustomTopicClick 点击自定义话题回调
 */
@Composable
fun PostCreateTopicChips(
    topics: List<String>,
    onTopicClick: (String) -> Unit,
    onCustomTopicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "#自定义话题",
            style = FontRegular(fontSize = 12, color = Color.White),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Colors.blue_277DFF)
                .clickable { onCustomTopicClick() }
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
        topics.forEach { topic ->
            Text(
                text = "#$topic",
                style = FontRegular(fontSize = 12, color = Colors.blue_277DFF),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Colors.gray_f0f4fe)
                    .clickable { onTopicClick(topic) }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
@Preview
private fun PostCreateTopicChipsPreview() {
    PostCreateTopicChips(
        topics = POST_CREATE_TOPICS,
        onTopicClick = {},
        onCustomTopicClick = {}
    )
}
