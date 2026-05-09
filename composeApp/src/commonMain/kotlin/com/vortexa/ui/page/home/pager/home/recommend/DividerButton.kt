package com.vortexa.ui.page.home.pager.home.recommend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.util.extension.click

/**
 *
 *
 *  @author LuXin
 *  @createTime 2026/2/5
 */
@Composable
fun DividerButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .height(25.dp)
            .then(
                if (onClick != null) Modifier.click(onClickListener = onClick) else Modifier
            )
    ) {

        Divider(
            modifier = Modifier.height(1.dp)
                .padding(horizontal = 18.dp)
                .background(color = Colors.gray_f0f4fe)
                .align(Alignment.Center)
        )

        Text(
            text,
            style = FontMedium(12, color = Colors.gray_B1B8C6),
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color.White)
                .padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun DividerButtonPreview() {
    DividerButton("显示更多导师")
}