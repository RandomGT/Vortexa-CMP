package com.vortexa.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

internal actual fun platformToast(message: String) {
    // First migration phase keeps toast as a no-op on iOS.
}

internal actual fun platformOpenUrl(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}

internal actual suspend fun platformPickImages(maxCount: Int): List<PickedMedia> = emptyList()
