package com.vortexa.platform

object AppToast {
    fun show(message: String) {
        platformToast(message)
    }
}

internal expect fun platformToast(message: String)
