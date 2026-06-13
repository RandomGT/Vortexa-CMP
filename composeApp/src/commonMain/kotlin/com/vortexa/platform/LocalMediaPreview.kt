package com.vortexa.platform

import androidx.compose.ui.graphics.ImageBitmap

/**
 * 从本地媒体 Uri（file://、content://）解码预览缩略图。
 * 相册/相机选图在 iOS 上写入临时文件，Coil 无法直接加载，需走平台解码。
 */
internal expect suspend fun platformLoadPreviewBitmap(uri: String, maxEdgePx: Int): ImageBitmap?

internal fun isLocalMediaUri(value: String): Boolean {
    val trimmed = value.trim()
    return trimmed.startsWith("file://") || trimmed.startsWith("content://")
}
