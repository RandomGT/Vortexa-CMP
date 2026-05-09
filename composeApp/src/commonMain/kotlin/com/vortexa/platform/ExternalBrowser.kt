package com.vortexa.platform

object ExternalBrowser {
    fun open(url: String) {
        platformOpenUrl(url)
    }
}

internal expect fun platformOpenUrl(url: String)
