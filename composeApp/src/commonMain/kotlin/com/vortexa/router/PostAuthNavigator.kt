package com.vortexa.router

import android.content.Context
import com.vortexa.navigation.AppRoute
import com.vortexa.navigation.NavigationRouteBridge

object PostAuthNavigator {
    fun navigateAfterLogin(context: Context, inlineAuth: Boolean = false) {
        NavigationRouteBridge.replaceRoot(AppRoute.Home())
    }

    fun navigateAfterRegister(context: Context) {
        NavigationRouteBridge.replaceRoot(AppRoute.Home())
    }
}
