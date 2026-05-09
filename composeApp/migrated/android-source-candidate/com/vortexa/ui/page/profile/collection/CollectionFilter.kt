package com.vortexa.ui.page.profile.collection

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular

/** 我的收藏、浏览记录等共用：全部 + 各分区展示名。 */
val collectionFilterChipLabels = listOf("全部", "杂谈", "交易经验", "玩法")

/** 与筛选 chip 顺序一致（index 0..3）；浏览记录等列表接口「全部」传 null，其余传对应 module 字符串。 */
val collectionFilterApiModuleParams: List<String?> =
    listOf(null) + collectionFilterChipLabels.drop(1)

/**
 * 我的收藏页筛选区（Figma 747-89856）：全部、杂谈、交易经验、玩法。
 * 选中态深底白字，未选中浅底深字。
 *
 * @param selectedIndex 当前选中索引，0=全部，1=杂谈，2=交易经验，3=玩法
 * @param onChipClick 点击某 Chip 回调
 */
@Composable
fun CollectionFilter(
    selectedIndex: Int,
    onChipClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val labels = collectionFilterChipLabels
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
@Preview
private fun CollectionFilterPreview() {
    CollectionFilter(selectedIndex = 0, onChipClick = {})
}
