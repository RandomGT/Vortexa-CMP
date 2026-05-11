package com.vortexa.ui.page.post.create

import android.app.Activity
import android.content.Intent
import com.vortexa.navigation.AppRoute
import com.vortexa.navigation.NavigationRouteBridge
import com.vortexa.navigation.encodeRouteStringList

class PostCreateActivity : Activity() {
    companion object {
        const val EXTRA_RESULT_POST_CREATED: String = "post_created"
        const val EXTRA_RESULT_POST_UPDATED: String = "post_updated"
        const val EXTRA_EDIT_POST_ID: String = "edit_post_id"
        const val EXTRA_EDIT_TITLE: String = "edit_title"
        const val EXTRA_EDIT_CONTENT: String = "edit_content"
        const val EXTRA_EDIT_MODULE: String = "edit_module"
        const val EXTRA_EDIT_MEDIA: String = "edit_media"

        private var pendingEditArgs: PostCreateEditArgs? = null

        fun parseEditArgs(intent: Intent): PostCreateEditArgs? = pendingEditArgs

        fun startForEdit(
            context: Any?,
            postId: String,
            title: String? = null,
            content: String? = null,
            board: String? = null,
            imageResources: List<String> = emptyList(),
            videoResources: List<String> = emptyList(),
        ) {
            val args = PostCreateEditArgs(
                postId = postId,
                title = title.orEmpty(),
                content = content.orEmpty(),
                board = board?.takeIf { it.isNotBlank() },
                imageResources = imageResources,
                videoResources = videoResources
            )
            pendingEditArgs = args
            NavigationRouteBridge.navigate(
                AppRoute.PostCreate(
                    editPostId = args.postId,
                    title = args.title,
                    content = args.content,
                    board = args.board.orEmpty(),
                    imageResourcesJson = encodeRouteStringList(args.imageResources),
                    videoResourcesJson = encodeRouteStringList(args.videoResources)
                )
            )
        }
    }
}

data class PostCreateEditArgs(
    val postId: String,
    val title: String,
    val content: String,
    val board: String? = null,
    val imageResources: List<String> = emptyList(),
    val videoResources: List<String> = emptyList(),
)
