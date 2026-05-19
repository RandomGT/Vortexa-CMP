package com.vortexa.ui.page.wallet

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vortexa.ui.page.home.pager.home.HomeHeaderTabItem
import org.jetbrains.compose.resources.painterResource
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.ic_tab_indicator

@Composable
fun WalletTabBar(
    selectedIndex: Int,
    onTabClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var tabPositions by remember { mutableStateOf(mapOf<Int, Dp>()) }
    var tabWidths by remember { mutableStateOf(mapOf<Int, Dp>()) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(start = 9.dp)
                .height(44.dp)
                .background(Color.White),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WalletTabs.forEachIndexed { index, title ->
                HomeHeaderTabItem(
                    text = title,
                    selected = index == selectedIndex,
                    onClick = { onTabClick(index) },
                    onPositioned = { x, w ->
                        with(density) {
                            tabPositions = tabPositions + (index to x.toDp())
                            tabWidths = tabWidths + (index to (w.toDp() + 15.dp))
                        }
                    }
                )
            }
        }

        if (tabPositions.isNotEmpty() && tabWidths.isNotEmpty()) {
            val currentTabX = tabPositions[selectedIndex] ?: 0.dp
            val currentTabWidth = tabWidths[selectedIndex] ?: 0.dp
            val indicatorWidth = 28.dp
            val targetOffset = currentTabX + (currentTabWidth - indicatorWidth) / 2
            val animatedOffset by animateDpAsState(targetValue = targetOffset)
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

