package com.vortexa.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium

private val ListEndDividerColor = Color(0xFFE4E7EB)

/**
 * 列表底部「到底了」提示：左右分割线 + 中间文案，供多页列表在无更多数据时统一展示。
 *
 * @param text 中间提示文案，默认「到底了」
 * @param modifier 外层修饰符，可传入 `padding(top = …)` 等与页面一致的间距
 */
@Composable
fun ListEndFooter(
    text: String = "到底了",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = ListEndDividerColor,
            thickness = 1.dp
        )
        Text(
            text = text,
            style = FontMedium(fontSize = 12, color = Colors.gray_667085),
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = ListEndDividerColor,
            thickness = 1.dp
        )
    }
}

@Composable
private fun ListEndFooterPreview() {
    ListEndFooter(modifier = Modifier.padding(16.dp))
}
