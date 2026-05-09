package com.vortexa.ui.page.search.history


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vortexa.ui.component.DefaultColorLabel
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click

private const val SEARCH_HISTORY_MAX_ROWS = 2
private val CHIP_VERTICAL_SPACING = 8.dp
private val CHIP_HEIGHT = 32.dp
private val HISTORY_SECTION_MAX_HEIGHT =
    (CHIP_HEIGHT + CHIP_VERTICAL_SPACING) * SEARCH_HISTORY_MAX_ROWS

/**
 * 搜索历史区域：标题「搜索历史」+ FlowRow 芯片（最多展示两行高度），支持清空。
 */
@Composable
fun SearchHistorySection(
    history: List<String>,
    onHistoryItemClick: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    if (history.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultColorLabel("搜索", "历史", modifier = Modifier.weight(1f))
            Text(
                text = "清空",
                style = FontRegular(fontSize = 14, color = Colors.gray_6A7282),
                modifier = Modifier.click(onClickListener = onClearHistory)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = HISTORY_SECTION_MAX_HEIGHT)
                .clip(RoundedCornerShape(0.dp))
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(CHIP_VERTICAL_SPACING)
            ) {
                history.forEach { keyword ->
                    SearchHistoryChip(
                        text = keyword,
                        onClick = { onHistoryItemClick(keyword) }
                    )
                }
            }
        }
    }
}
