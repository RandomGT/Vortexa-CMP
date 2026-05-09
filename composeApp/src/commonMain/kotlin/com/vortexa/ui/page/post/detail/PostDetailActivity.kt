package com.vortexa.ui.page.post.detail

import com.vortexa.model.Post
import com.vortexa.navigation.AppRoute
import com.vortexa.navigation.NavigationRouteBridge

object PostDetailActivity {
    const val EXTRA_POST_ID: String = "post_id"
    const val EXTRA_OPEN_REPLY_COMPOSER: String = "open_reply_composer"
    fun start(context: Any?, post: Post, openReplyComposer: Boolean = false) {
        start(context, post.id, openReplyComposer)
    }

    fun start(context: Any?, postId: String, openReplyComposer: Boolean = false) {
        NavigationRouteBridge.navigate(AppRoute.PostDetail(postId, openReplyComposer))
    }

    fun startForReplyToComment(
        context: Any?,
        postId: String,
        commentId: Long,
        authorName: String,
        content: String,
        avatar: Any? = null,
    ) {
        NavigationRouteBridge.navigate(AppRoute.PostDetail(postId, openReplyComposer = true))
    }
}
