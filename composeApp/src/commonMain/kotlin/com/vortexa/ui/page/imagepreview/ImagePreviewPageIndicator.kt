package com.vortexa.ui.page.imagepreview

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 图片预览页面指示器：圆点样式，当前页高亮。
 * 设计：选中=白色实心 8dp，未选中=半透明白色 6dp，带 150ms 动画过渡。
 *
 * @param totalCount 总页数
 * @param currentIndex 当前页索引
 */
@Composable
fun ImagePreviewPageIndicator(
    totalCount: Int,
    currentIndex: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalCount) { index ->
            val isSelected = index == currentIndex
            val sizeDp by animateDpAsState(
                targetValue = if (isSelected) 8.dp else 6.dp,
                animationSpec = tween(150),
                label = "indicator_size"
            )
            val alpha = if (isSelected) 1f else 0.5f

            Box(
                modifier = Modifier
                    .size(sizeDp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = alpha))
            )
        }
    }
}

@Composable
private fun ImagePreviewPageIndicatorPreview() {
    ImagePreviewPageIndicator(
        totalCount = 5,
        currentIndex = 2
    )
}
