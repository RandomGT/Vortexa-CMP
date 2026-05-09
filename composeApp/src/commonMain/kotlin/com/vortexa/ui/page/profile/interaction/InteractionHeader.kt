package com.vortexa.ui.page.profile.interaction

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
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.ic_arrow_left
import vortexa.composeapp.generated.resources.icon_menu

/**
 * 互动管理页头部（Figma 504-50409）：白底、左返回箭头、居中标题「互动管理」、右侧菜单图标。
 *
 * @param onBackClick 点击返回回调
 * @param onMenuClick 点击右侧菜单回调
 */
@Composable
fun InteractionHeader(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit = {},
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
            text = "互动管理",
            style = FontMedium(fontSize = 16, color = Colors.black_101828),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            textAlign = TextAlign.Center
        )
        Icon(
            painter = painterResource(Res.drawable.icon_menu),
            contentDescription = "菜单",
            tint = Colors.black_101828,
            modifier = Modifier
                .size(24.dp)
                .click(onClickListener = onMenuClick)
        )
    }
}

@Composable
private fun InteractionHeaderPreview() {
    InteractionHeader(onBackClick = {}, onMenuClick = {})
}
