package com.vortexa.util.extension

import com.vortexa.navigation.NavigationRouteBridge
import kotlin.reflect.KClass

fun Any?.routeToPage(target: KClass<*>) {
    NavigationRouteBridge.routeToPage(target)
}
