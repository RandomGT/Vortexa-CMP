package com.vortexa.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

/**
 * desc : Infinite Scrolling Banner with Custom Indicator
 *
 * @author LuXin
 * @createTime 2026/1/21
 */
@Composable
fun <T> Banner(
    data: List<T>,
    modifier: Modifier = Modifier,
    autoScrollInterval: Long = 3000,
    itemContent: @Composable BoxScope.(T) -> Unit
) {
    if (data.isEmpty()) return

    // Use a large number for infinite scrolling effect
    val pageCount = Int.MAX_VALUE
    // Start from the middle to allow scrolling both ways
    val startIndex = pageCount / 2 - (pageCount / 2) % data.size
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { pageCount })

    // Auto scroll logic
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.isScrollInProgress }
            .collectLatest { isScrolling ->
                if (!isScrolling) {
                    while (true) {
                        delay(autoScrollInterval)
                        try {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        } catch (e: Exception) {
                            // Handle cancellation or other errors
                        }
                    }
                }
            }
    }

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val item = data[page % data.size]
            Box(modifier = Modifier.fillMaxSize()) {
                itemContent(item)
            }
        }

        // Custom Indicator
        BannerIndicator(
            totalCount = data.size,
            currentIndex = pagerState.currentPage % data.size,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 148.dp, bottom = 6.dp) // Position from Figma
        )
    }
}

@Composable
fun BannerIndicator(
    totalCount: Int,
    currentIndex: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalCount) { index ->
            val isSelected = index == currentIndex
            val width by animateDpAsState(targetValue = if (isSelected) 22.dp else 8.dp)
            val alpha = if (isSelected) 1f else 0.5f

            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(width)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = alpha))
            )
        }
    }
}
