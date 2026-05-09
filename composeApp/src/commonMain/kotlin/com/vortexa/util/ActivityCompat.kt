package com.vortexa.util

import android.app.Activity
import com.vortexa.navigation.NavigationRouteBridge
import kotlin.reflect.KClass

fun Any?.findActivity(): Activity = this as? Activity ?: Activity()

fun Any?.routeToPage(target: KClass<*>) {
    NavigationRouteBridge.routeToPage(target)
}
