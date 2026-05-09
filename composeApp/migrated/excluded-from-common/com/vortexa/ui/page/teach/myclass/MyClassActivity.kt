package com.vortexa.ui.page.teach.myclass

import androidx.compose.runtime.Composable
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.theme.BaseTheme

class MyClassActivity: BaseActivity() {
    @Composable
    override fun ContentPage() {
        BaseTheme(belowStatusBar = true, aboveNavigationBar = true) {
            MyClassView(onBackClick = { finish() })
        }
    }

}