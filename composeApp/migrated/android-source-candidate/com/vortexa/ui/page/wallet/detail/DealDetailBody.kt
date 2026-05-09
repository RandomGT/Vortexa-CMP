package com.vortexa.ui.page.wallet.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.ui.theme.FontSemiBold

/**
 * 交易详情主体（Figma 336-14716 ~ 14746）：支付状态区 + 「交易明细」标题 + 明细行列表。
 *
 * @param statusText 状态文案，如「支付成功」
 * @param amountDisplay 金额展示，如「$100」
 * @param rows 明细行（label, value）
 */
@Composable
fun DealDetailBody(
    statusText: String,
    amountDisplay: String,
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp)
    ) {
        // 状态区：支付成功 + 金额（Figma 336-14717/14718）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = statusText,
                style = FontSemiBold(fontSize = 18, color = Colors.blue_277DFF)
            )
            Text(
                text = amountDisplay,
                style = FontSemiBold(fontSize = 48, color = Colors.blue_277DFF)
            )
        }
        // 交易明细标题（Figma 336-14721）
        Text(
            text = "交易明细",
            style = FontSemiBold(fontSize = 18, color = Colors.black_101828),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 6.dp)
        )
        // 明细行（Figma 336-14726 等）
        rows.forEach { (label, value) ->
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
private fun DealDetailBodyPreview() {
    DealDetailBody(
        statusText = "支付成功",
        amountDisplay = "$100",
        rows = listOf(
            "当前状态" to "完成",
            "订单金额" to "120.00",
            "优惠折扣" to "-20.00",
            "支付时间" to "2025-10-08 16:00",
            "支付方式" to "支付宝",
            "商品说明" to "积分充值",
            "订单号" to "16456549649612121"
        )
    )
}
