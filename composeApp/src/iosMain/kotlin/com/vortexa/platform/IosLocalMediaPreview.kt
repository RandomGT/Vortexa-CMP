package com.vortexa.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image as SkiaImage
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
internal actual suspend fun platformLoadPreviewBitmap(uri: String, maxEdgePx: Int): ImageBitmap? =
    withContext(Dispatchers.Default) {
        val url = uri.toPreviewNSURL() ?: return@withContext null
        val path = url.path ?: return@withContext null
        val data = NSFileManager.defaultManager.contentsAtPath(path) ?: return@withContext null
        val bytes = data.toByteArrayOrNull() ?: return@withContext null
        val image = SkiaImage.makeFromEncoded(bytes) ?: return@withContext null
        image.toComposeImageBitmap()
    }

private fun String.toPreviewNSURL(): NSURL? {
    val value = trim()
    if (value.isEmpty()) return null
    return when {
        value.startsWith("file://") -> NSURL.URLWithString(value)
        value.startsWith("/") -> NSURL.fileURLWithPath(value)
        else -> NSURL.URLWithString(value)?.takeIf { it.isFileURL() }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArrayOrNull(): ByteArray? {
    val length = this.length.toInt()
    val source = bytes ?: return null
    val target = ByteArray(length)
    if (length == 0) return target
    target.usePinned { pinned ->
        memcpy(pinned.addressOf(0), source, this.length.convert())
    }
    return target
}
