package com.vortexa.ui.page.profile.focus

import androidx.compose.runtime.Composable
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.theme.BaseTheme

class MyFocusActivity: BaseActivity() {
    @Composable
    override fun ContentPage() {
        BaseTheme(belowStatusBar = true, aboveNavigationBar = true) {
            MyFocusView(onBackClick = { finish() })
        }
    }
}