package com.vortexa.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Loading 指示器尺寸，对应 iOS [UIActivityIndicatorView] 的 Medium / Large 样式。
 */
enum class LoadingIndicatorSize {
    /** 行内小菊花，约 16–20pt */
    Small,
    /** 列表底部加载更多，约 20pt */
    Medium,
    /** 全屏/区块居中加载，约 37pt */
    Large,
}

/**
 * 平台原生样式的 Loading 指示器。
 * iOS 上使用系统 [UIActivityIndicatorView]（菊花转圈）。
 */
@Composable
expect fun AppLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    size: LoadingIndicatorSize = LoadingIndicatorSize.Medium,
)
