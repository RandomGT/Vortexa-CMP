package com.vortexa.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.posix.memcpy

internal actual suspend fun platformReadUploadFile(uri: String): PlatformUploadFile? {
    val url = uri.toNSURL() ?: return null
    val path = url.path ?: return null
    val data = NSFileManager.defaultManager.contentsAtPath(path) ?: return null
    val bytes = data.toByteArrayOrNull() ?: return null
    val fileName = url.lastPathComponent?.takeIf { it.isNotBlank() } ?: "post_upload.jpg"
    return PlatformUploadFile(
        bytes = bytes,
        fileName = fileName,
        contentType = fileName.imageContentType()
    )
}

private fun String.toNSURL(): NSURL? {
    val value = trim()
    if (value.isEmpty()) return null
    return when {
        value.startsWith("file://") -> NSURL.URLWithString(value)
        value.startsWith("/") -> NSURL.fileURLWithPath(value)
        else -> NSURL.URLWithString(value)?.takeIf { it.isFileURL() }
    }
}

private fun String.imageContentType(): String {
    return when (substringAfterLast('.', "").lowercase()) {
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        "heic" -> "image/heic"
        "heif" -> "image/heif"
        else -> "image/jpeg"
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
