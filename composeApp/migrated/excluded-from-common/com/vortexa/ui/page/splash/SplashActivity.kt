package com.vortexa.ui.page.splash

import androidx.compose.runtime.Composable
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.page.home.HomeActivity
import com.vortexa.util.extension.routeToPage

class SplashActivity : BaseActivity() {
    @Composable
    override fun ContentPage() {
        SplashPage {
            routeToPage(HomeActivity::class.java)
            finish()
        }
    }
}