package com.vortexa.ui.page.post.create

import android.net.Uri

data class PostCreateSelectedMedia(
    val uri: Uri,
    val type: PostCreateMediaType,
    val isRemote: Boolean = false,
)

enum class PostCreateMediaType {
    Image,
    Video,
}
