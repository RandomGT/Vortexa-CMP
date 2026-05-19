package com.vortexa.ui.page.teach.schedule.confirm

import androidx.compose.foundation.background
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
 * 订单确认页底部双按钮（Figma 415-40809~40813）：返回修改（灰底）、确认订单（黑底白字）。
 *
 * @param onModifyClick 返回修改
 * @param onConfirmClick 确认订单
 */
@Composable
fun ConfirmBottomBar(
    onModifyClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(30.dp))
                .background(Colors.gray_EEF0F1)
                .click( onModifyClick)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(
                text = "返回修改",
                style = FontMedium(fontSize = 16, color = Colors.black_101828)
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(30.dp))
                .background(Colors.black_101828)
                .click(onConfirmClick)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(
                text = "确认订单",
                style = FontMedium(fontSize = 16, color = Color.White)
            )
        }
    }
}

@Composable
@Preview
private fun ConfirmBottomBarPreview() {
    ConfirmBottomBar(onModifyClick = {}, onConfirmClick = {})
}
