package com.vortexa.ui.page.wallet.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.util.extension.click

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
        DetailActionButton("往来记录", Colors.gray_EEF0F1, Colors.black_101828, onRecordClick, rowShape)
        DetailActionButton("订单疑问", Colors.gray_EEF0F1, Colors.black_101828, onQuestionClick, rowShape)
        DetailActionButton("联系商家", Colors.black_101828, Color.White, onContactClick, rowShape)
    }
}

@Composable
private fun RowScope.DetailActionButton(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    shape: RoundedCornerShape
) {
    Row(
        modifier = Modifier
            .weight(1f)
            .clip(shape)
            .background(backgroundColor)
            .padding(vertical = 10.dp)
            .click(onClickListener = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = FontMedium(fontSize = 16, color = contentColor))
    }
}
