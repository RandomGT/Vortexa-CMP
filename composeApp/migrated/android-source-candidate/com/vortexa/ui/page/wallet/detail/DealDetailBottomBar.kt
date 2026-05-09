package com.vortexa.ui.page.wallet.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.vortexa.util.extension.click

/**
 * 交易详情页底部操作栏（Figma 336-14751）：往来记录、订单疑问（灰底）、联系商家（深色主按钮）。
 *
 * @param onRecordClick 往来记录
 * @param onQuestionClick 订单疑问
 * @param onContactClick 联系商家
 */
@Composable
fun DealDetailBottomBar(
    onRecordClick: () -> Unit = {},
    onQuestionClick: () -> Unit = {},
    onContactClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val rowShape = RoundedCornerShape(30.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(rowShape)
                .background(Colors.gray_EEF0F1)
                .padding(vertical = 10.dp)
                .click(onClickListener = onRecordClick),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "往来记录",
                style = FontMedium(fontSize = 16, color = Colors.black_101828)
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(rowShape)
                .background(Colors.gray_EEF0F1)
                .padding(vertical = 10.dp)
                .click(onClickListener = onQuestionClick),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "订单疑问",
                style = FontMedium(fontSize = 16, color = Colors.black_101828)
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(rowShape)
                .background(Colors.black_101828)
                .padding(vertical = 10.dp)
                .click(onClickListener = onContactClick),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "联系商家",
                style = FontMedium(fontSize = 16, color = Color.White)
            )
        }
    }
}

@Composable
@Preview
private fun DealDetailBottomBarPreview() {
    DealDetailBottomBar()
}
