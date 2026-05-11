package com.vortexa.platform

internal expect fun authSessionGetString(key: String, defaultValue: String = ""): String
internal expect fun authSessionGetLong(key: String, defaultValue: Long = 0L): Long
internal expect fun authSessionPutString(key: String, value: String?)
internal expect fun authSessionPutLong(key: String, value: Long)
internal expect fun authSessionRemove(key: String)
