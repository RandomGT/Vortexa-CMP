package com.vortexa.ui.page.profile.interaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular

/**
 * 互动管理页筛选区（Figma 504-50419）：横向 Filter Chips，选中深底白字，未选浅底深字。
 *
 * @param labels 筛选项文案列表，如 ["所有人", "我的关注"]
 * @param selectedIndex 当前选中索引
 * @param onChipClick 点击某 Chip 回调
 */
@Composable
fun InteractionFilter(
    labels: List<String>,
    selectedIndex: Int,
    onChipClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val bg = if (selected) Colors.black_101828 else Colors.gray_f0f4fe
            val textColor = if (selected) Color.White else Colors.black_101828
            val textStyle = if (selected) FontMedium(fontSize = 12, color = textColor)
            else FontRegular(fontSize = 12, color = textColor)
            Text(
                text = label,
                style = textStyle,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
                    .clickable { onChipClick(index) }
            )
        }
    }
}

@Composable
private fun InteractionFilterPreview() {
    InteractionFilter(
        labels = listOf("所有人", "我的关注"),
        selectedIndex = 0,
        onChipClick = {}
    )
}
