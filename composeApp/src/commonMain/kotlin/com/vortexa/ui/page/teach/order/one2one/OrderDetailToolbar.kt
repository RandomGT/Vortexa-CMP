package com.vortexa.ui.page.teach.order.one2one

import androidx.compose.foundation.background
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
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.ic_arrow_left

/**
 * 订单详情页顶部栏：左侧返回、居中「预约回执」。
 */
@Composable
fun OrderDetailToolbar(
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
            modifier = Modifier.size(24.dp).click(onBackClick)
        )
        Text(
            text = "预约回执",
            style = FontMedium(fontSize = 16, color = Colors.black_101828),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            textAlign = TextAlign.Center
        )
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_left),
            contentDescription = null,
            tint = Color.Transparent,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
@Preview
private fun OrderDetailToolbarPreview() {
    OrderDetailToolbar(onBackClick = {})
}
