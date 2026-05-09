package com.vortexa.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.BaseTheme

/**
 * 可折叠头部 Pager 容器：头部先折叠，再滚动当前页列表；下拉时反向展开头部。
 *
 * @param pagerState 外部传入的 PagerState，用于与 TabBar 双向同步。
 * @param pageCount Pager 页数，用于为每页持有独立的列表滚动状态。
 * @param modifier 外层修饰符。
 * @param headerContent 可整体上滑收起的头部内容，如搜索栏、Banner、筛选区。
 * @param stickyContent 吸顶内容，如 TabBar。
 * @param pageContent 每页内容；第二个参数为当前页对应的 LazyListState。
 */
@Composable
fun CollapsibleHeaderPagerScaffold(
    pagerState: PagerState,
    pageCount: Int,
    modifier: Modifier = Modifier,
    headerContent: @Composable () -> Unit,
    stickyContent: @Composable () -> Unit,
    pageContent: @Composable (page: Int, listState: LazyListState) -> Unit
) {
    // 每个分页持有独立列表状态，避免切页后滚动位置互相串用。
    val pageListStates = remember(pageCount) {
        List(pageCount) { LazyListState() }
    }
    var headerHeightPx by remember { mutableFloatStateOf(0f) }
    var headerOffsetPx by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember(pageCount, pagerState) {
        object : NestedScrollConnection {
            /**
             * 处理向上预滚动：优先消耗位移来折叠头部，头部完全收起后再交给子列表继续滚动。
             *
             * @param available 当前可供消费的滚动位移。
             * @param source 滚动来源。
             * @return 当前连接消费掉的位移。
             */
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y >= 0f || headerHeightPx <= 0f) return Offset.Zero
                val consumedY = consumeHeaderDelta(
                    deltaY = available.y,
                    currentOffsetPx = headerOffsetPx,
                    headerHeightPx = headerHeightPx
                )
                if (consumedY == 0f) return Offset.Zero
                headerOffsetPx += consumedY
                return Offset(x = 0f, y = consumedY)
            }

            /**
             * 处理子列表滚动后的剩余位移：仅当当前页列表已经回到顶部时，才把剩余下拉位移用于展开头部。
             *
             * @param consumed 子节点已消费的位移。
             * @param available 子节点未消费、可继续向父节点上传的位移。
             * @param source 滚动来源。
             * @return 当前连接消费掉的位移。
             */
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y <= 0f || headerHeightPx <= 0f) return Offset.Zero
                val currentListState = pageListStates.getOrNull(pagerState.currentPage) ?: return Offset.Zero
                if (!currentListState.isScrolledToTop()) return Offset.Zero
                val consumedY = consumeHeaderDelta(
                    deltaY = available.y,
                    currentOffsetPx = headerOffsetPx,
                    headerHeightPx = headerHeightPx
                )
                if (consumedY == 0f) return Offset.Zero
                headerOffsetPx += consumedY
                return Offset(x = 0f, y = consumedY)
            }
        }
    }

    Column(
        modifier = modifier.nestedScroll(nestedScrollConnection)
    ) {
        // 自定义测量头部完整高度，再通过父布局裁剪实现“视觉折叠”，而不是重新压缩头部内容本身。
        Layout(
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds(),
            content = headerContent
        ) { measurables, constraints ->
            val headerPlaceable = measurables.firstOrNull()?.measure(
                constraints.copy(minWidth = constraints.maxWidth, minHeight = 0)
            )
            val measuredHeaderHeight = headerPlaceable?.height ?: 0
            if (measuredHeaderHeight > 0 && headerHeightPx != measuredHeaderHeight.toFloat()) {
                headerHeightPx = measuredHeaderHeight.toFloat()
                headerOffsetPx = headerOffsetPx.coerceIn(-headerHeightPx, 0f)
            }
            val visibleHeaderHeight = (measuredHeaderHeight + headerOffsetPx.toInt())
                .coerceIn(0, measuredHeaderHeight)
            layout(
                width = constraints.maxWidth,
                height = visibleHeaderHeight
            ) {
                headerPlaceable?.placeRelative(
                    x = 0,
                    y = headerOffsetPx.toInt()
                )
            }
        }

        stickyContent()

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            beyondViewportPageCount = pageCount,
            userScrollEnabled = true
        ) { page ->
            pageContent(page, pageListStates[page])
        }
    }
}

/**
 * 根据滚动位移更新头部偏移量，保证头部始终位于“完全展开”到“完全收起”之间。
 *
 * @param deltaY 本次待消费的纵向位移；向上为负，向下为正。
 * @param currentOffsetPx 当前头部偏移量。
 * @param headerHeightPx 头部完整高度。
 * @return 实际应由头部消费的位移。
 */
private fun consumeHeaderDelta(
    deltaY: Float,
    currentOffsetPx: Float,
    headerHeightPx: Float
): Float {
    val nextOffsetPx = (currentOffsetPx + deltaY).coerceIn(-headerHeightPx, 0f)
    return nextOffsetPx - currentOffsetPx
}

/**
 * 判断列表是否已经完全回到顶部。
 *
 * @return 顶部位置返回 true，否则返回 false。
 */
private fun LazyListState.isScrolledToTop(): Boolean {
    return firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0
}

/**
 * 预览可折叠头部 Pager 容器的联动布局结构。
 *
 * @return 无返回值。
 */
@Composable
private fun CollapsibleHeaderPagerScaffoldPreview() {
    BaseTheme {
        val pagerState = rememberPagerState(pageCount = { 2 })
        CollapsibleHeaderPagerScaffold(
            pagerState = pagerState,
            pageCount = 2,
            headerContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    Text(text = "SearchBar", modifier = Modifier.padding(16.dp))
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(Color(0xFFE8EEF9))
                    )
                }
            },
            stickyContent = {
                Text(
                    text = "Sticky TabBar",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F7FA))
                        .padding(16.dp)
                )
            }
        ) { page, listState ->
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(20) { index ->
                    Text(
                        text = "Page $page Item $index",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}
