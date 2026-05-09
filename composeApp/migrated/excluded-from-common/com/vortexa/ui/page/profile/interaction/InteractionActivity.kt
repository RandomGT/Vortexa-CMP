package com.vortexa.ui.page.profile.interaction

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
class InteractionActivity : BaseActivity() {
    @Composable
    override fun ContentPage() {
        BaseTheme(aboveNavigationBar = true, belowStatusBar = true) {
            InteractionView(onBackClick = { finish() })
        }
    }

}