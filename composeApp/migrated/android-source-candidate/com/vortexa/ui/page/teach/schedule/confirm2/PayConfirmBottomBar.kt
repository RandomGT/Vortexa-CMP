package com.vortexa.ui.page.teach.schedule.confirm2

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.component.LoadingButton
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.util.extension.click

/**
 * 支付确认页底部双按钮（Figma 422-36622~36626）：返回（灰底）、确认支付（LoadingButton，请求中显示 loading）。
 */
@Composable
fun PayConfirmBottomBar(
    payLoading: Boolean,
    onBackClick: () -> Unit,
    onPayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(30.dp))
                .background(Colors.gray_EEF0F1)
                .click(onBackClick)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "返回", style = FontMedium(fontSize = 16, color = Colors.black_101828))
        }
        LoadingButton(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(30.dp))
                .background(Colors.black_101828)
                .padding(vertical = 10.dp),
            text = "确认支付",
            isLoading = payLoading,
            onClick = onPayClick
        )
    }
}

@Composable
@Preview
private fun PayConfirmBottomBarPreview() {
    PayConfirmBottomBar(payLoading = false, onBackClick = {}, onPayClick = {})
}
