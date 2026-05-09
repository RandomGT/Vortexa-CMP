package com.vortexa.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular

/** 下拉浮窗圆角、内边距（与 MyClassSchoolFilterPopup 统一） */
private val popupShape = RoundedCornerShape(8.dp)
private val popupPadding = 12.dp
private val itemVerticalPadding = 10.dp

/**
 * 自定义下拉菜单浮窗，与项目 UI 风格一致（白底、8dp 圆角、阴影）。
 *
 * @param expanded 是否展开
 * @param onDismissRequest 关闭回调
 * @param options 选项文案列表
 * @param onOptionClick 点击某项回调，参数为选项索引
 * @param modifier 修饰符
 */
@Composable
fun PopupDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    options: List<String>,
    onOptionClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!expanded) return

    val density = LocalDensity.current
    // 在锚点下方显示：TopStart + 下偏移（约图标高度 + 4dp 间距）
    val offsetY = with(density) { (32.dp).roundToPx() }

    Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset(0, offsetY),
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = modifier.widthIn(min = 147.dp),
            shape = popupShape,
            color = Color.White,
            shadowElevation = 10.dp,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                options.forEachIndexed { index, label ->
                    Text(
                        text = label,
                        style = FontRegular(fontSize = 14, color = Colors.black_101828),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { onOptionClick(index) }
                            .padding(
                                horizontal = popupPadding,
                                vertical = itemVerticalPadding
                            )
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun PopupDropdownMenuPreview() {
    var expanded by remember { mutableStateOf(true) }
    Box(
        modifier = Modifier.padding(32.dp),
        contentAlignment = Alignment.TopStart
    ) {
        PopupDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            options = listOf("全部", "学习进度", "已下架", "已上架"),
            onOptionClick = { expanded = false }
        )
    }
}
