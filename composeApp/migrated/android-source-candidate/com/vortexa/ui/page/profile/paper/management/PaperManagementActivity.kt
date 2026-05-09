package com.vortexa.ui.page.profile.paper.management

import androidx.compose.runtime.Composable
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.theme.BaseTheme

class PaperManagementActivity: BaseActivity() {
    @Composable
    override fun ContentPage() {
        BaseTheme(aboveNavigationBar = true, belowStatusBar = true) {
            PaperManagementView()
        }
    }

}