package com.vortexa.ui.page.profile.history

import androidx.compose.runtime.Composable
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.theme.BaseTheme

/**
 * 浏览记录页 Activity。
 *
 * @author LuXin
 */
class HistoryActivity : BaseActivity() {

    @Composable
    override fun ContentPage() {
        BaseTheme(aboveNavigationBar = true, belowStatusBar = true) {
            HistoryView(onBackClick = { finish() })
        }
    }
}
