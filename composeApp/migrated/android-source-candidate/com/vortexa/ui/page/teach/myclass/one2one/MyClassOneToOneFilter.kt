package com.vortexa.ui.page.teach.myclass.one2one

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular

/** 一对一服务筛选选项：与接口 `status` 参数对应，由服务端筛选 */
val MyClassOneToOneFilterOptions =
    listOf("全部", "待接受", "待完成", "已取消", "已完成")

/**
 * 一对一服务列表上方 Filter（Figma 336-14923）：横向 Chip，选中深底白字，未选浅底深字。
 *
 * @param selectedIndex 当前选中项下标
 * @param onOptionClick 点击某项回调
 */
@Composable
fun MyClassOneToOneFilter(
    selectedIndex: Int,
    onOptionClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MyClassOneToOneFilterOptions.forEachIndexed { index, label ->
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
}

@Composable
@Preview
private fun MyClassOneToOneFilterPreview() {
    MyClassOneToOneFilter(selectedIndex = 0, onOptionClick = {})
}
