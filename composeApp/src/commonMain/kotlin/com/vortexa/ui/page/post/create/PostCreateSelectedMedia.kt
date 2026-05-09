package com.vortexa.ui.page.post.create

import android.net.Uri

data class PostCreateSelectedMedia(
    val uri: Uri,
    val type: PostCreateMediaType,
)

enum class PostCreateMediaType {
    Image,
    Video,
}
