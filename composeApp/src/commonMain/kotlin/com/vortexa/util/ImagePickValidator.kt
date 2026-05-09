package com.vortexa.util

import android.net.Uri

object ImagePickValidator {
    sealed class Result {
        data object Ok : Result()
        data class Invalid(val reason: String = "") : Result()
    }

    fun filterValidImageUris(context: Any?, uris: List<Uri>): Pair<List<Uri>, Result?> =
        uris to null

    fun validate(context: Any?, uri: Uri): Result = Result.Ok

    fun toastMessage(result: Result): String = when (result) {
        Result.Ok -> ""
        is Result.Invalid -> result.reason.ifBlank { "图片不可用" }
    }
}
