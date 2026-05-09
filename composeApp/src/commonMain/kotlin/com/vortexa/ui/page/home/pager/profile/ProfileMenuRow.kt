package com.vortexa.ui.page.home.pager.profile


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import org.jetbrains.compose.resources.DrawableResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.eye
import vortexa.composeapp.generated.resources.light
import vortexa.composeapp.generated.resources.msg
import vortexa.composeapp.generated.resources.rect

/**
 * 功能入口行（Figma 746-69802）：创作中心、互动管理、我的收藏、浏览记录，
 * 圆角浅灰背景，图标+文字，使用 light/msg/rect/eye drawable。
 */
@Composable
fun ProfileMenuRow(
    modifier: Modifier = Modifier,
    onItemClick: (String, Int) -> Unit
) {
    val items = listOf(
        "创作中心" to Res.drawable.light,
        "互动管理" to Res.drawable.msg,
        "我的收藏" to Res.drawable.rect,
        "浏览记录" to Res.drawable.eye
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Colors.gray_F8F9FA, RoundedCornerShape(12.dp))
            .padding(vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEachIndexed { index, (title, iconRes) ->
                ProfileMenuItem(
                    title = title,
                    iconRes = iconRes,
                    onClick = { onItemClick(title, index) }
                )
            }
        }
    }
}

/**
 * 单个功能入口：图标在上，文字在下，居中。
 */
@Composable
private fun ProfileMenuItem(
    title: String,
    iconRes: DrawableResource,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .click(onClick)
            .height(52.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = title,
            modifier = Modifier.size(28.dp),
            tint = Color.Unspecified
        )
        Text(
            text = title,
            style = FontRegular(fontSize = 12, Colors.black_242424),
            modifier = Modifier.padding(top = 8.dp),
            lineHeight = 16.sp
        )
    }
}
