package com.vortexa.ui.page.search.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import com.vortexa.ui.component.DefaultColorLabel
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.topic

/** Figma 设计：热搜话题每行 8dp 间距 */
private val ROW_SPACING = 12.dp
/** Figma：话题 # 图标与文本间距 8dp */
private val ICON_TEXT_GAP = 8.dp

/** 热搜话题最多展示条数 */
private const val HOT_TOPIC_MAX_COUNT = 3

/**
 * 热搜话题区域（Figma 747-85971）：标题「热搜话题」+ 列表形式，每行含图标 + 话题文本。
 * 最多展示 3 条，仅图标有热度渐变（opacity 100%/50%/30%），文字保持不透明。
 *
 * @param topics 热搜话题列表
 * @param onTopicClick 点击话题时的回调，参数为话题文本
 */
@Composable
fun HotTopicSection(
    topics: List<String>,
    onTopicClick: (String) -> Unit
) {
    if (topics.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
    ) {
        DefaultColorLabel("热搜", "话题", showMore = false, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ROW_SPACING)
        ) {
            topics.take(HOT_TOPIC_MAX_COUNT).forEachIndexed { index, topic ->
                HotTopicRow(
                    topic = topic,
                    alpha = when (index) {
                        0 -> 1f
                        1 -> 0.5f
                        else -> 0.3f
                    },
                    onClick = { onTopicClick(topic) }
                )
            }
        }
    }
}

/**
 * 单行热搜话题：话题图标（仅图标有热度 opacity）+ 文本。
 */
@Composable
private fun HotTopicRow(
    topic: String,
    alpha: Float,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ICON_TEXT_GAP)
    ) {
        Icon(
            painter = painterResource(Res.drawable.topic),
            contentDescription = null,
            modifier = Modifier.size(16.dp).alpha(alpha),
            tint = Colors.blue_277DFF
        )
        Text(
            text = topic,
            style = FontRegular(fontSize = 15, color = Colors.black_101828),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HotTopicSectionPreview() {
    HotTopicSection(
        topics = listOf("量化交易", "短线策略", "DeFi 玩法", "空投指南", "趋势跟踪"),
        onTopicClick = {}
    )
}
