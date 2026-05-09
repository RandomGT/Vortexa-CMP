package com.vortexa.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import vortexa.composeapp.generated.resources.Res

/**
 * 星级评分组件：展示 5 颗星，按 [litCount] 从左到右渐进点亮黄色星（支持小数，如 4.5 为四颗满星 + 半颗）。
 *
 * @param litCount 评分 0f..5f，超出范围会被裁剪；非数字（NaN）按 0 处理
 * @param modifier 布局修饰符
 * @param starSize 单颗星尺寸，默认 12.dp
 */
@Composable
fun StarRating(
    litCount: Float,
    modifier: Modifier = Modifier,
    starSize: Dp = 12.dp
) {
    val rating = when {
        litCount.isNaN() -> 0f
        else -> litCount.coerceIn(0f, 5f)
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(5) { index ->
            // 第 index 颗星（0 起）的填充比例：整颗为 1，半颗为 0.5，未点亮的为 0
            val fillFraction = (rating - index).coerceIn(0f, 1f)
            Box(modifier = Modifier.size(starSize)) {
                Image(
                    painter = painterResource(Res.drawable.icon_star),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                if (fillFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxHeight()
                            .width(starSize * fillFraction)
                            .clip(RectangleShape)
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.icon_star_light),
                            contentDescription = null,
                            modifier = Modifier
                                .size(starSize)
                                .align(Alignment.CenterStart)
                        )
                    }
                }
            }
        }
    }
}
