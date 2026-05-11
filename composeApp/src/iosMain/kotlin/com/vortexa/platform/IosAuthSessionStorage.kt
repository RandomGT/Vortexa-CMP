package com.vortexa.platform

import platform.Foundation.NSUserDefaults

private val authSessionDefaults: NSUserDefaults
    get() = NSUserDefaults.standardUserDefaults

internal actual fun authSessionGetString(key: String, defaultValue: String): String {
    return authSessionDefaults.stringForKey(key) ?: defaultValue
}

internal actual fun authSessionGetLong(key: String, defaultValue: Long): Long {
    if (authSessionDefaults.objectForKey(key) == null) return defaultValue
    return authSessionDefaults.integerForKey(key)
}

internal actual fun authSessionPutString(key: String, value: String?) {
    if (value == null) {
        authSessionRemove(key)
    } else {
        authSessionDefaults.setObject(value, key)
        authSessionDefaults.synchronize()
    }
}

internal actual fun authSessionPutLong(key: String, value: Long) {
    authSessionDefaults.setInteger(value, key)
    authSessionDefaults.synchronize()
}

internal actual fun authSessionRemove(key: String) {
    authSessionDefaults.removeObjectForKey(key)
    authSessionDefaults.synchronize()
}
