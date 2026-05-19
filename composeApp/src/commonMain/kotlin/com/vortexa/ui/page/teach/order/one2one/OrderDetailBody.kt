package com.vortexa.ui.page.teach.order.one2one

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.ui.theme.FontSemiBold

/**
 * 订单详情页主体（Figma 336-14191~14221）：「记录详情」标题 + 明细行列表；不包含打赏按钮。
 *
 * @param detailRows 明细行（label, value），如状态、订单价格、实付价格等
 */
@Composable
fun OrderDetailBody(
    detailRows: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp)
    ) {
        Text(
            text = "记录详情",
            style = FontSemiBold(fontSize = 18, color = Colors.black_101828),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 6.dp)
        )
        detailRows.forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
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
private fun OrderDetailBodyPreview() {
    OrderDetailBody(
        detailRows = listOf(
            "状态" to "已完成",
            "订单价格" to "120.00",
            "实付价格" to "100.00",
            "下单时间" to "2025-10-08 16:00",
            "课程时间" to "2025-10-08 20:00",
            "课程时长" to "2小时",
            "订单编号" to "16456549649612121",
            "支付方式" to "积分"
        )
    )
}
