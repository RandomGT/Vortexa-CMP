package com.vortexa

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIColor

fun MainViewController() = ComposeUIViewController { App() }.also {
    it.view.backgroundColor = UIColor.whiteColor
}
