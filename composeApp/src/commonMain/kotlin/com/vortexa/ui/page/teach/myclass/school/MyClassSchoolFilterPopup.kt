package com.vortexa.ui.page.teach.myclass.school

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click

/** 浮窗圆角、内边距、阴影（Figma 768-52158 / 768-52133） */
private val popupShape = RoundedCornerShape(8.dp)
private val popupPadding = 12.dp
private val itemGap = 16.dp
private val iconTextGap = 4.dp
private val iconSize = 16.dp

/** 学员样式选项（Figma 768-52158） */
val SchoolFilterPopupOptionsStudent = listOf("全部", "学习进度")

/** 导师样式选项（Figma 768-52133） */
val SchoolFilterPopupOptionsTutor = listOf("全部", "已下架", "已上架", "销量")

/**
 * 涡联学院 Filter 浮窗（Figma 768-52158 学员 / 768-52133 导师）
 * 白底、8dp 圆角、阴影，纵向选项列表，选中项为蓝色勾选图标，未选为灰色空心圆。
 *
 * @param options 选项文案列表（学员 2 项 / 导师 4 项）
 * @param selectedIndex 当前选中下标
 * @param onOptionClick 点击某项回调，一般选中并关闭浮窗
 */
@Composable
fun MyClassSchoolFilterPopup(
    options: List<String>,
    selectedIndex: Int,
    onOptionClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(100.dp),
        shape = popupShape,
        color = Color.White,
        shadowElevation = 10.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(popupPadding),
            verticalArrangement = Arrangement.spacedBy(itemGap)
        ) {
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
                    FilterPopupRadioIcon(selected = selected)
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
private fun FilterPopupRadioIcon(
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
                // 白色勾：左下到中下再到右上
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
private fun MyClassSchoolFilterPopupPreview() {
    MyClassSchoolFilterPopup(
        options = SchoolFilterPopupOptionsTutor,
        selectedIndex = 0,
        onOptionClick = {}
    )
}
