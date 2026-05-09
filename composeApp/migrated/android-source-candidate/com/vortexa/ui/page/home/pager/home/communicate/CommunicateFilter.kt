package com.vortexa.ui.page.home.pager.home.communicate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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

/** 帖子分区标签顺序对应 postType 1~4：综合、杂谈、交易经验、玩法 */
val COMMUNICATE_FILTER_LABELS = listOf("综合", "杂谈", "交易经验", "玩法")

/**
 * 将帖子上的板块文案（如 `#杂谈`、`杂谈`）映射为交流区接口 [postType]（1~4）；无法匹配时视为综合。
 */
fun moduleLabelToCommunicatePostType(moduleRaw: String?): Int {
    val s = moduleRaw?.trim()?.removePrefix("#")?.trim().orEmpty()
    if (s.isEmpty()) return 1
    val idx = COMMUNICATE_FILTER_LABELS.indexOfFirst { it == s }
    return if (idx >= 0) idx + 1 else 1
}

/**
 * 交流页筛选条（Figma 747-82877）。
 * 水平 Chip：选中深底白字，未选中浅底深字，圆角 8dp，间距 10dp。
 *
 * @param selectedPostType 当前选中的分区，与接口一致：1~4
 * @param onSelect 选中某一项时回调，参数为 postType（1~4）
 */
@Composable
fun CommunicateFilter(
    selectedPostType: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        COMMUNICATE_FILTER_LABELS.forEachIndexed { index, label ->
            val postType = index + 1
            val selected = postType == selectedPostType
            Text(
                text = label,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (selected) Colors.black_101828
                        else Colors.gray_f0f4fe
                    )
                    .clickable(
                        interactionSource = MutableInteractionSource(),
                        indication = null,
                        onClick = { onSelect(postType) }
                    )
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                style = if (selected) {
                    FontMedium(fontSize = 12, color = Color.White)
                } else {
                    FontRegular(fontSize = 12, color = Colors.black_101828)
                }
            )
        }
    }
}

@Composable
@Preview
fun CommunicateFilterPreview() {
    CommunicateFilter(selectedPostType = 1, onSelect = {})
}
