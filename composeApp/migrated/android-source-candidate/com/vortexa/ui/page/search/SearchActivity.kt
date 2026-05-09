package com.vortexa.ui.page.search

import androidx.compose.runtime.Composable
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.theme.BaseTheme

class SearchActivity : BaseActivity() {
    @Composable
    override fun ContentPage() {
        BaseTheme(belowStatusBar = true, aboveNavigationBar = true) {
            SearchView(onBack = { finish() })
        }
    }
}