package com.vortexa.ui.page.creator.statistics

import androidx.compose.runtime.Composable
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.theme.BaseTheme

/**
 * 数据中心页 Activity（Figma 504-51125）。
 * 从创作者中心「数据中心」入口进入。
 */
class DataCenterActivity : BaseActivity() {

    @Composable
    override fun ContentPage() {
        BaseTheme(belowStatusBar = true, aboveNavigationBar = true) {
            DataCenterView(onBackClick = { finish() })
        }
    }
}
