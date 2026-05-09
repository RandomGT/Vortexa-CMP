package com.vortexa.ui.page.teach.schedule.confirm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.ui.theme.FontSemiBold

/**
 * 订单确认页「预约详情」区块（Figma 415-40776~40804）：标题 + 多行 label-value。
 *
 * @param rows 每行 (label, value)
 */
@Composable
fun ConfirmDetailSection(
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "预约详情",
            style = FontSemiBold(fontSize = 18, color = Colors.black_101828),
            modifier = Modifier
                .padding(top = 24.dp, bottom = 6.dp)
                .padding(horizontal = 18.dp)
        )
        rows.forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = FontRegular(fontSize = 15, color = Colors.gray_6A7282)
                )
                Text(
                    text = value,
                    style = FontSemiBold(fontSize = 15, color = Colors.black_101828)
                )
            }
        }
    }
}

@Composable
@Preview
private fun ConfirmDetailSectionPreview() {
    ConfirmDetailSection(
        rows = listOf(
            "状态" to "进行中",
            "订单价格" to "120.00",
            "实付价格" to "100.00"
        )
    )
}
