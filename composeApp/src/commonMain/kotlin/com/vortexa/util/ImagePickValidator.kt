package com.vortexa.util

import android.net.Uri

object ImagePickValidator {
    const val MAX_LONG_EDGE_PX = 4096
    const val MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L

    private val supportedExtensions = setOf("jpg", "jpeg", "png", "webp", "gif", "heic", "heif")

    sealed class Result {
        data object Ok : Result()
        data class Invalid(val reason: String = "") : Result()
        data class UnsupportedFormat(val extension: String) : Result()
    }

    fun filterValidImageUris(context: Any?, uris: List<Uri>): Pair<List<Uri>, Result?> {
        if (uris.isEmpty()) return emptyList<Uri>() to null
        val ok = ArrayList<Uri>(uris.size)
        var firstBad: Result? = null
        for (uri in uris) {
            when (val result = validate(context, uri)) {
                Result.Ok -> ok.add(uri)
                else -> if (firstBad == null) firstBad = result
            }
        }
        return ok to firstBad
    }

    fun validate(context: Any?, uri: Uri): Result {
        val value = uri.toString().trim()
        if (value.isEmpty()) return Result.Invalid("图片不可用")

        val lower = value.substringBefore('?').substringBefore('#').lowercase()
        val lastSegment = lower.substringAfterLast('/')
        val extension = lastSegment.substringAfterLast('.', missingDelimiterValue = "")
        if (extension.isNotEmpty() && extension !in supportedExtensions) {
            return Result.UnsupportedFormat(extension)
        }
        return Result.Ok
    }

    fun toastMessage(result: Result): String = when (result) {
        Result.Ok -> ""
        is Result.Invalid -> result.reason.ifBlank { "图片不可用" }
        is Result.UnsupportedFormat -> "仅支持 JPG、PNG、WEBP、GIF、HEIC 图片"
    }
}
