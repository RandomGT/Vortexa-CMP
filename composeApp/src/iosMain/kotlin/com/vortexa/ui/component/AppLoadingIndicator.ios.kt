package com.vortexa.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.UIKitView
import platform.UIKit.UIActivityIndicatorView
import platform.UIKit.UIActivityIndicatorViewStyleLarge
import platform.UIKit.UIActivityIndicatorViewStyleMedium
import platform.UIKit.UIColor

@Composable
actual fun AppLoadingIndicator(
    modifier: Modifier,
    color: Color,
    size: LoadingIndicatorSize,
) {
    val style = when (size) {
        LoadingIndicatorSize.Small,
        LoadingIndicatorSize.Medium -> UIActivityIndicatorViewStyleMedium
        LoadingIndicatorSize.Large -> UIActivityIndicatorViewStyleLarge
    }
    UIKitView(
        factory = {
            UIActivityIndicatorView(style).apply {
                hidesWhenStopped = false
                if (color != Color.Unspecified) {
                    this.color = color.toUiColor()
                }
                startAnimating()
            }
        },
        modifier = modifier,
        update = { indicator ->
            if (!indicator.isAnimating()) {
                indicator.startAnimating()
            }
            if (color != Color.Unspecified) {
                indicator.color = color.toUiColor()
            }
        },
    )
}

private fun Color.toUiColor(): UIColor =
    UIColor.colorWithRed(
        red = red.toDouble(),
        green = green.toDouble(),
        blue = blue.toDouble(),
        alpha = alpha.toDouble(),
    )
