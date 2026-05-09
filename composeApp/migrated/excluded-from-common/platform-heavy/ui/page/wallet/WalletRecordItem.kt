package com.vortexa.ui.page.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.ui.theme.FontSemiBold
import vortexa.composeapp.generated.resources.Res

/**
 * 单条积分记录行（Figma 336-21375）：日期 | 金额 | 操作文案 + 右箭头。
 *
 * @param date 日期，如 "2025-10-02"
 * @param amount 金额，如 "+50"
 * @param action 操作类型，如 "充值"
 */
@Composable
fun WalletRecordItem(
    date: String,
    amount: String,
    action: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date,
            style = FontRegular(fontSize = 15, color = Colors.gray_6A7282),
            modifier = Modifier
        )
        Text(
            text = amount,
            style = FontSemiBold(fontSize = 15, color = Colors.black_101828),
            modifier = Modifier
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = action,
                style = FontSemiBold(fontSize = 15, color = Colors.black_101828)
            )
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_right_gray),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Colors.black_101828
            )
        }
    }
}

@Composable
private fun WalletRecordItemPreview() {
    WalletRecordItem(
        date = "2025-10-02",
        amount = "+50",
        action = "充值"
    )
}
