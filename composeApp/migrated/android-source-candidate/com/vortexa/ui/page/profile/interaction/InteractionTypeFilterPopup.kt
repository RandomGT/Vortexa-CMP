package com.vortexa.ui.page.profile.interaction

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click

/** 浮层圆角、内边距、阴影（Figma 504-50384） */
private val popupShape = RoundedCornerShape(8.dp)
private val popupPadding = 12.dp
private val titleItemGap = 16.dp
private val iconTextGap = 4.dp
private val iconSize = 16.dp

/** 回复 Tab 下的互动类型选项 */
val InteractionTypeOptionsReply = listOf("全部", "回复我的", "我回复的")

/** 点赞 Tab 下的互动类型选项 */
val InteractionTypeOptionsLike = listOf("全部", "收到的点赞", "我的点赞")

/**
 * 互动类型筛选浮层（Figma 504-50384）
 * 白底、8dp 圆角、阴影，标题「互动类型」+ 单选列表，选中项为蓝色勾选图标，未选为灰色空心圆。
 *
 * @param options 选项文案列表（回复 Tab：全部/回复我的/我回复的；点赞 Tab：全部/收到的点赞/我的点赞）
 * @param selectedIndex 当前选中下标
 * @param onOptionClick 点击某项回调，一般选中并关闭浮窗
 * @param modifier 修饰符
 */
@Composable
fun InteractionTypeFilterPopup(
    options: List<String>,
    selectedIndex: Int,
    onOptionClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.width(120.dp),
        shape = popupShape,
        color = Color.White,
        shadowElevation = 10.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(popupPadding),
            verticalArrangement = Arrangement.spacedBy(titleItemGap)
        ) {
            Text(
                text = "互动类型",
                style = FontMedium(fontSize = 14, color = Colors.black_101828)
            )
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .click { onOptionClick(index) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(iconTextGap)
                ) {
                    InteractionTypeFilterRadioIcon(selected = selected)
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
private fun InteractionTypeFilterRadioIcon(
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
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
private fun InteractionTypeFilterPopupPreview() {
    InteractionTypeFilterPopup(
        options = InteractionTypeOptionsReply,
        selectedIndex = 0,
        onOptionClick = {}
    )
}
