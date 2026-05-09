package com.vortexa.ui.page.search.result

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vortexa.ui.page.home.pager.home.HomeHeaderTabItem
import com.vortexa.ui.theme.Colors
import com.vortexa.util.extension.throttleClick
import kotlin.collections.plus
import vortexa.composeapp.generated.resources.Res

/** 搜索结果的 6 个 Tab 文案，与 Figma 设计一致 */
val SearchResultTabs = listOf("综合", "帖文", "用户", "导师", "工具箱", "课程")

/** Tab 索引对应的接口 type 参数 */
val SearchResultTypes = listOf("general", "post", "user", "teacher", "toolbox", "course")

/** 「帖文」对应下标，与 [SearchResultTypes] 中 `"post"` 一致 */
const val SEARCH_RESULT_POST_TAB_INDEX = 1

/**
 * 搜索结果页 TabBar：水平排列多个 Tab，选中项为深色文字 + 蓝色下划线指示器。
 *
 * @param selectedIndex 当前选中的 Tab 索引
 * @param onTabClick 点击 Tab 的回调，参数为 Tab 索引
 */
@Composable
fun SearchResultTabBar(
    selectedIndex: Int,
    onTabClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    // Store the x position of each tab
    var tabPositions by remember { mutableStateOf(mapOf<Int, Dp>()) }
    // Store the width of each tab to center the indicator
    var tabWidths by remember { mutableStateOf(mapOf<Int, Dp>()) }
    Box {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(Color.White),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchResultTabs.forEachIndexed { index, title ->
                SearchResultTabItem(
                    index = index,
                    title = title,
                    selected = index == selectedIndex,
                    onClick = { onTabClick(index) },
                    onPositioned = { x, width ->
                        with(density) {
                            val xDp = x.toDp()
                            val widthDp = width.toDp()
                            tabPositions = tabPositions + (index to xDp)
                            tabWidths = tabWidths + (index to widthDp)
                        }
                    }
                )
            }
        }

        // Sliding Indicator
        // Only show if we have positions
        if (tabPositions.isNotEmpty() && tabWidths.isNotEmpty()) {
            val currentTabX = tabPositions[selectedIndex] ?: 0.dp
            val currentTabWidth = tabWidths[selectedIndex] ?: 0.dp

            // Calculate center position for the 28.dp indicator
            // Indicator is 28.dp wide.
            // Target X = TabX + (TabWidth - IndicatorWidth) / 2
            val indicatorWidth = 28.dp
            val targetOffset = currentTabX + (currentTabWidth - indicatorWidth) / 2

            val animatedOffset by animateDpAsState(targetValue = targetOffset)

            // Row 高度 44.dp、内容垂直居中，文字+Spacer(6.dp)≈26.dp，故 (44-26)/2 + 26 = 35.dp 为指示器顶部
            Image(
                painter = painterResource(Res.drawable.ic_tab_indicator),
                contentDescription = "Indicator",
                modifier = Modifier
                    .offset(x = animatedOffset, y = 35.dp)
                    .width(indicatorWidth)
                    .height(5.dp)
            )
        }
    }
}


/**
 * 单个 Tab 项：文字 + 选中时底部蓝色指示条。
 *
 * @param title Tab 文案
 * @param selected 是否选中
 * @param onClick 点击回调
 */
@Composable
private fun SearchResultTabItem(
    index: Int,
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    onPositioned: (Float, Int) -> Unit
) {
    val density = LocalDensity.current

    HomeHeaderTabItem(
        text = title,
        selected = selected,
        onClick = { onClick() },
        onPositioned = onPositioned
    )
}
