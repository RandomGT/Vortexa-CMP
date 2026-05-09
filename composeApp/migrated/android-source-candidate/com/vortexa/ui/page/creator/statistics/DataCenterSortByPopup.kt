package com.vortexa.ui.page.creator.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular

/**
 * sortBy 排序选项（对应 GET /v/api/user/posts/data/{days} 的 sortBy 参数）
 */
val DataCenterSortByOptions = listOf(
    0 to "默认",
    1 to "最多点击",
    2 to "最多回复",
    3 to "最多点赞",
)

/**
 * 贴文排序选择弹窗（Figma 504-51100）：白底、8dp 圆角、阴影，多项单选。
 *
 * @param selectedSortBy 当前选中的 sortBy 值（与接口 sortBy 一致）
 * @param onSortBySelect 选择某项回调
 * @param modifier 修饰符
 */
@Composable
fun DataCenterSortByPopup(
    selectedSortBy: Int,
    onSortBySelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 10.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DataCenterSortByOptions.forEach { (value, label) ->
                val selected = value == selectedSortBy
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSortBySelect(value) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DataCenterSortByRadioIcon(selected = selected)
                    Text(
                        text = label,
                        style = FontRegular(fontSize = 14, color = Colors.black_101828)
                    )
                }
            }
        }
    }
}

/**
 * 单选图标：选中为蓝色实心圆+白勾，未选为灰色空心圆（Figma check-contained）
 */
@Composable
fun DataCenterSortByRadioIcon(
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val iconSize = 16.dp
    androidx.compose.foundation.layout.Box(
        modifier = modifier.size(iconSize),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Canvas(modifier = Modifier.size(iconSize)) {
                drawCircle(color = Colors.blue_277DFF)
                val path = Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.55f)
                    lineTo(size.width * 0.42f, size.height * 0.75f)
                    lineTo(size.width * 0.82f, size.height * 0.25f)
                }
                drawPath(path, Color.White, style = Stroke(width = 2f))
            }
        } else {
            Canvas(modifier = Modifier.size(iconSize)) {
                drawCircle(
                    color = Colors.gray_B1B8C6,
                    style = Stroke(width = 1.5f)
                )
            }
        }
    }
}

@Composable
@Preview
private fun DataCenterSortByPopupPreview() {
    DataCenterSortByPopup(
        selectedSortBy = 3,
        onSortBySelect = {}
    )
}
