package com.vortexa.ui.page.creator

import androidx.compose.runtime.Composable
import com.vortexa.ui.theme.BaseTheme

class CreatorCenterActivity

/**
 * 创作者中心页面入口。导航层接入时只需要把三个子页面跳转回调传入即可。
 */
@Composable
fun CreatorCenterPage(
    onBackClick: () -> Unit,
    onDataCenterClick: () -> Unit,
    onInteractionClick: () -> Unit,
    onPaperManagementClick: () -> Unit,
) {
    BaseTheme(belowStatusBar = false, aboveNavigationBar = true) {
        CreatorCenterView(
            onBackClick = onBackClick,
            onDataCenterClick = onDataCenterClick,
            onInteractionClick = onInteractionClick,
            onPaperManagementClick = onPaperManagementClick,
        )
    }
}
