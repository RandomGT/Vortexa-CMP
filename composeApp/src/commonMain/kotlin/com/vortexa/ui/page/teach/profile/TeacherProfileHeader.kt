package com.vortexa.ui.page.teach.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.icon_back

/**
 * 教师资料页顶部栏（Figma 283-30351）：左侧返回箭头，右侧「个人主页」胶囊按钮。
 *
 * @param onBackClick 点击返回
 * @param onProfileClick 点击「个人主页」
 */
@Composable
fun TeacherProfileHeader(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Res.drawable.icon_back),
            contentDescription = "返回",
            tint = Color.White,
            modifier = Modifier
                .size(24.dp)
                .click(onClickListener = onBackClick),
        )
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier
                .background(
                    color = androidx.compose.ui.graphics.Color.White,
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 20.dp, vertical = 5.dp)
                .click(onClickListener = onProfileClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "个人主页",
                style = FontMedium(fontSize = 14, color = Colors.black_101828)
            )
        }
    }
}
