package com.vortexa.ui.page.wallet

import androidx.compose.runtime.Composable
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.theme.BaseTheme

/**
 *  desc : TODO Fill the fucking desc
 *
 *
 *  @author LuXin
 *  @createTime 2026/2/27
 */
class WalletActivity : BaseActivity() {
    // PRIVATE METHODS

    // PUBLIC METHODS
    @Composable
    override fun ContentPage() {
        BaseTheme(belowStatusBar = false, aboveNavigationBar = true) {
            WalletView(onBackClick = { finish() })
        }
    }
}