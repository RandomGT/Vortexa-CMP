package com.vortexa.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.util.extension.throttleClick
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.home
import vortexa.composeapp.generated.resources.icon_bottom_start
import vortexa.composeapp.generated.resources.message
import vortexa.composeapp.generated.resources.profile
import vortexa.composeapp.generated.resources.school

/**
 *  desc : 首页Tab组件
 *
 *
 *  @author LuXin
 *  @createTime 2026/1/20
 */
@Composable
fun HomeTab(
    icon: DrawableResource,
    selected: Boolean = false,
    showMessageTip: Boolean = false,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .throttleClick(onClickListener = onClick),
    ) {
        Image(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.Center),
            painter = painterResource(icon),
            contentDescription = "tab",
//            colorFilter = ColorFilter.tint(Color.Red)
            colorFilter = ColorFilter.tint(if (selected) Colors.black_101828 else Colors.gray_B1B8C6)
        )


        if (showMessageTip) {
            Box(
                modifier = Modifier
                    .padding(end = 6.dp, top = 6.dp)
                    .size(6.dp)
                    .background(Color.Red, shape = CircleShape)
                    .align(Alignment.TopEnd)
            )
        }
    }
}

/**
 *  desc : 首页Tab组件
 *  @param modifier
 *  @param selected 当前选中的Tab索引
 *  @param messageTip 显示消息提示的Tab索引
 *  @param onClick 点击Tab的回调
 */
@Composable
fun HomeTabContent(
    modifier: Modifier,
    selected: Int = 0,
    messageTip: List<Int> = emptyList(),
    onClick: (Int) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeTab(
            Res.drawable.home,
            selected == 0,
            messageTip.contains(0),
            onClick = { onClick(0) })
        HomeTab(
            Res.drawable.message,
            selected == 1,
            messageTip.contains(1),
            onClick = { onClick(1) })
        HomeTab(
            Res.drawable.school,
            selected == 2,
            messageTip.contains(2),
            onClick = { onClick(2) })
        HomeTab(
            Res.drawable.icon_bottom_start,
            selected == 3,
            messageTip.contains(3),
            onClick = { onClick(3) })
        HomeTab(
            Res.drawable.profile,
            selected == 4,
            messageTip.contains(4),
            onClick = { onClick(4) })
    }
}

@Composable
fun HomeTabPreview() {
    HomeTabContent(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color.White),
        messageTip = listOf(0, 1, 2),
        onClick = {

        }
    )
}
