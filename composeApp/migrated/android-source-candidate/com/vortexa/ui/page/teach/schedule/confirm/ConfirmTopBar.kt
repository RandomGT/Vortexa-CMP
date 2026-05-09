package com.vortexa.ui.page.teach.schedule.confirm

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res

/**
 * 订单确认页顶部栏（Figma 415-40750）：左侧返回、居中「订单确认」、右侧客服与分享占位。
 *
 * @param onBackClick 返回点击
 * @param onServiceClick 客服点击，可选
 * @param onShareClick 分享点击，可选
 */
@Composable
fun ConfirmTopBar(
    onBackClick: () -> Unit,
    onServiceClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
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
                .click(onBackClick)
        )
        Text(
            text = "订单确认",
            style = FontMedium(fontSize = 16, color = Colors.black_101828),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            textAlign = TextAlign.Center
        )
//        Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(24.dp)) {
//            Box(
//                modifier = Modifier
//                    .size(24.dp)
//                    .then(
//                        if (onServiceClick != null) Modifier.click(onServiceClick)
//                        else Modifier
//                    )
//            ) { /* 客服图标占位 */ }
//            Box(
//                modifier = Modifier
//                    .size(24.dp)
//                    .then(
//                        if (onShareClick != null) Modifier.click( onShareClick)
//                        else Modifier
//                    )
//            ) { /* 分享图标占位 */ }
//        }
    }
}

@Composable
@Preview
private fun ConfirmTopBarPreview() {
    ConfirmTopBar(onBackClick = {})
}
