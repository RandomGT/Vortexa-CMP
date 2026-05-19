package com.vortexa.ui.page.teach.myclass

import com.vortexa.navigation.AppRoute
import com.vortexa.navigation.NavigationRouteBridge

object MyClassActivity {
    fun start(context: Any?) {
        NavigationRouteBridge.navigate(AppRoute.MyClass)
    }
}
