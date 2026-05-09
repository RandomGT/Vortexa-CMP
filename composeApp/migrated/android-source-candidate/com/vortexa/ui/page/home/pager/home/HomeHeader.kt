package com.vortexa.ui.page.home.pager.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res

@Composable
fun HomeHeader(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    onSearchClick: () -> Unit
) {
    val density = LocalDensity.current
    // Store the x position of each tab
    var tabPositions by remember { mutableStateOf(mapOf<Int, Dp>()) }
    // Store the width of each tab to center the indicator
    var tabWidths by remember { mutableStateOf(mapOf<Int, Dp>()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .padding(bottom = 8.dp), // Extra padding from Figma
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tabs Container
        Box {
            // Tab Items Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeHeaderTabItem(
                    text = "推荐",
                    selected = currentTab == 0,
                    onClick = { onTabSelected(0) },
                    onPositioned = { x, width ->
                        with(density) {
                            val xDp = x.toDp()
                            val widthDp = width.toDp()
                            tabPositions = tabPositions + (0 to xDp)
                            tabWidths = tabWidths + (0 to widthDp)
                        }
                    }
                )
                HomeHeaderTabItem(
                    text = "交流",
                    selected = currentTab == 1,
                    onClick = { onTabSelected(1) },
                    onPositioned = { x, width ->
                        with(density) {
                            val xDp = x.toDp()
                            val widthDp = width.toDp()
                            tabPositions = tabPositions + (1 to xDp)
                            tabWidths = tabWidths + (1 to widthDp)
                        }
                    }
                )
            }

            // Sliding Indicator
            // Only show if we have positions
            if (tabPositions.isNotEmpty() && tabWidths.isNotEmpty()) {
                val currentTabX = tabPositions[currentTab] ?: 0.dp
                val currentTabWidth = tabWidths[currentTab] ?: 0.dp
                
                // Calculate center position for the 28.dp indicator
                // Indicator is 28.dp wide. 
                // Target X = TabX + (TabWidth - IndicatorWidth) / 2
                val indicatorWidth = 28.dp
                val targetOffset = currentTabX + (currentTabWidth - indicatorWidth) / 2

                val animatedOffset by animateDpAsState(targetValue = targetOffset)

                Image(
                    painter = painterResource(Res.drawable.ic_tab_indicator),
                    contentDescription = "Indicator",
                    modifier = Modifier
                        .offset(x = animatedOffset, y = 26.dp) // Adjust y to be below text. Text height approx 22-24dp + spacer.
                        .width(indicatorWidth)
                        .height(5.dp)
                )
            }
        }

        // Search Icon
        Image(
            painter = painterResource(Res.drawable.icon_search),
            contentDescription = "Search",
            modifier = Modifier
                .size(24.dp)
                .click(onClickListener = onSearchClick)
        )
    }
}

@Composable
fun HomeHeaderTabItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    onPositioned: (Float, Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .click(onClick)
            .onGloballyPositioned { coordinates ->
                // We need the position relative to the parent Row (or Box)
                // However, onGloballyPositioned gives global coords or local.
                // To get relative to parent, we might need to know parent's position or use a different approach.
                // Simpler approach: Just use the width and index since we know the spacing (24.dp).
                // But text width varies.
                // Let's rely on positionInParent if possible, but Compose doesn't give that directly easily without BoxScope.
                // Actually `coordinates.positionInParent()` exists!
                onPositioned(coordinates.positionInParent().x, coordinates.size.width)
            }
    ) {
        Text(
            text = text,
            style = FontMedium(16, Colors.black_101828),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(6.dp)) // Space for indicator (1dp gap + 5dp height)
    }
}
