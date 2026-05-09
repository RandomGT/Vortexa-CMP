package com.vortexa.ui.page.post.detail

data class PostDetailEditPayload(
    val title: String,
    val content: String,
    val imageResources: List<String>,
    val videoResources: List<String>,
)

