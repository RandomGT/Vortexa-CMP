package com.vortexa.ui.page.teach.schedule.confirm2

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
 * 支付确认页「预约详情」区块（Figma 422-36598~36619）：指导时长、指导费用、优惠券、积分余额、总计。
 */
@Composable
fun PayConfirmDetailSection(
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
                Text(text = label, style = FontRegular(fontSize = 15, color = Colors.gray_6A7282))
                Text(text = value, style = FontSemiBold(fontSize = 15, color = Colors.black_101828))
            }
        }
    }
}

@Composable
@Preview
private fun PayConfirmDetailSectionPreview() {
    PayConfirmDetailSection(
        rows = listOf(
            "指导时长" to "2小时",
            "指导费用" to "120积分",
//            "优惠券" to "-20积分",
            "积分余额" to "200积分",
            "总计" to "100积分"
        )
    )
}
