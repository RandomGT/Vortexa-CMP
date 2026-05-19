package com.vortexa.ui.page.teach.myclass.school

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.icon_menu

/** 一对一服务筛选选项（Figma 336-14923 文案：全部、进行中、已完成） */
val MyClassSchoolFilterOptions = listOf("知识读物", "视频教学")

/**
 * 一对一服务列表上方 Filter（Figma 336-14923）：横向 Chip + 右侧菜单按钮。
 *
 * @param selectedIndex 当前选中项下标
 * @param onOptionClick 点击某项回调
 * @param onMenuClick 点击右侧菜单图标回调，用于打开学员/导师浮窗
 */
@Composable
fun MyClassSchoolFilter(
    selectedIndex: Int,
    onOptionClick: (Int) -> Unit,
    onMenuClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f)
            .horizontalScroll(rememberScrollState())) {
            MyClassSchoolFilterOptions.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val backgroundColor = if (selected) Colors.black_101828 else Colors.gray_f0f4fe
                val textColor = if (selected) Color.White else Colors.black_101828
                val textStyle = if (selected) FontMedium(fontSize = 12, color = textColor)
                else FontRegular(fontSize = 12, color = textColor)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor)
                        .clickable(onClick = { onOptionClick(index) })
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = label, style = textStyle)
                }
            }
        }

        Icon(
            painter = painterResource(Res.drawable.icon_menu),
            contentDescription = null,
            modifier = Modifier.click { onMenuClick() }
        )

    }
}

@Composable
@Preview
private fun MyClassSchoolFilterPreview() {
    MyClassSchoolFilter(selectedIndex = 0, onOptionClick = {})
}
