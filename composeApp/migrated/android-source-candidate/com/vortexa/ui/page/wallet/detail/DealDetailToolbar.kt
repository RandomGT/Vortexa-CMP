package com.vortexa.ui.page.wallet.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res

/**
 * 交易详情页顶部导航栏（Figma 336-14700）：白底、左侧返回箭头、居中标题「交易详情」。
 *
 * @param onBackClick 点击返回回调
 */
@Composable
fun DealDetailToolbar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_left),
            contentDescription = "返回",
            tint = Colors.black_101828,
            modifier = Modifier
                .size(24.dp)
                .click(onClickListener = onBackClick)
        )
        Text(
            text = "交易详情",
            style = FontMedium(fontSize = 16, color = Colors.black_101828),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        // 右侧占位，与设计稿右侧空白（opacity 0）对称
        Box(modifier = Modifier.size(24.dp)) {}
    }
}

@Composable
@Preview
private fun DealDetailToolbarPreview() {
    DealDetailToolbar(onBackClick = {})
}
