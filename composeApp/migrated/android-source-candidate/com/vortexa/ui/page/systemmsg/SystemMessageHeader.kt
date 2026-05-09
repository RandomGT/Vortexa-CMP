package com.vortexa.ui.page.systemmsg

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res

/**
 * 系统通知页头部（Figma 747-90324）：左侧返回 + 标题，右侧菜单图标。
 *
 * @param title 导航标题，如「系统通知」「课堂小助手」
 * @param onBackClick 点击返回箭头
 * @param onMenuClick 点击右侧菜单图标
 */
@Composable
fun SystemMessageHeader(
    title: String,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.icon_back),
                contentDescription = "返回",
                modifier = Modifier
                    .size(24.dp)
                    .click(onClickListener = onBackClick),
                tint = Colors.black_101828
            )
            Text(
                text = title,
                style = FontMedium(fontSize = 16, color = Colors.black_101828)
            )
        }
    }
}
