package com.vortexa.ui.page.home

import com.vortexa.config.TokenConfig
import com.vortexa.navigation.AppRoute
import com.vortexa.navigation.NavigationRouteBridge

object HomeGuestTabLogin {
    fun openGuestLoginInsteadOfTab(context: Any?, tab: Int): Boolean {
        if (TokenConfig.getToken().isNotEmpty() || tab == 0 || tab == 2) {
            return false
        }
        return NavigationRouteBridge.navigate(AppRoute.Login)
    }
}
