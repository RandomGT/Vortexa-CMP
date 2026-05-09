package com.vortexa.session

import com.vortexa.navigation.AppRoute
import com.vortexa.navigation.NavigationRouteBridge

object AuthNavGate {
    fun reset() {
        NavigationRouteBridge.replaceRoot(AppRoute.Login)
    }
}
