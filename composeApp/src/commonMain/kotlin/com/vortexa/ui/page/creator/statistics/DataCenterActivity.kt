package com.vortexa.ui.page.creator.statistics

import androidx.compose.runtime.Composable
import com.vortexa.ui.theme.BaseTheme

class DataCenterActivity

@Composable
fun DataCenterPage(
    onBackClick: () -> Unit,
    onPostClick: (Long) -> Unit,
) {
    BaseTheme(belowStatusBar = true, aboveNavigationBar = true) {
        DataCenterView(
            onBackClick = onBackClick,
            onPostClick = onPostClick,
        )
    }
}
