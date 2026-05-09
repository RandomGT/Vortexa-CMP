package com.vortexa.ui.page.imagepreview

import com.vortexa.navigation.AppRoute
import com.vortexa.navigation.NavigationRouteBridge
import com.vortexa.navigation.encodeRouteStringList

object ImagePreviewActivity {
    fun start(context: Any?, urls: List<String>, initialIndex: Int = 0) {
        NavigationRouteBridge.navigate(
            AppRoute.ImagePreview(
                urlsJson = encodeRouteStringList(urls),
                initialIndex = initialIndex,
            )
        )
    }
}
