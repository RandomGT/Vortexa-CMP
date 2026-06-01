package com.vortexa.platform

data class PickedMedia(
    val uri: String,
    val type: MediaType,
)

enum class MediaType {
    Image,
    Video,
}

object MediaPicker {
    suspend fun pickImages(maxCount: Int): List<PickedMedia> = platformPickImages(maxCount)

    suspend fun takePhoto(): PickedMedia? = platformTakePhoto()

    suspend fun pickVideo(): PickedMedia? = platformPickVideo()
}

internal expect suspend fun platformPickImages(maxCount: Int): List<PickedMedia>

internal expect suspend fun platformTakePhoto(): PickedMedia?

internal expect suspend fun platformPickVideo(): PickedMedia?
