package com.vortexa.ui.page.login.forget

import androidx.compose.runtime.Composable
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.theme.BaseTheme

class ForgetActivity: BaseActivity() {
    @Composable
    override fun ContentPage() {
        BaseTheme(belowStatusBar = false, aboveNavigationBar = true) {
            ForgetView()
        }
    }
}