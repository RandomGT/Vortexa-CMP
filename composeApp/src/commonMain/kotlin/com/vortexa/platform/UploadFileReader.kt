package com.vortexa.platform

data class PlatformUploadFile(
    val bytes: ByteArray,
    val fileName: String,
    val contentType: String,
)

internal expect suspend fun platformReadUploadFile(uri: String): PlatformUploadFile?
