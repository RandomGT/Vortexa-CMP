package com.vortexa.ui.page.post.detail

import com.vortexa.model.Post

object PostDetailActivity {
    const val EXTRA_POST_ID: String = "post_id"
    const val EXTRA_OPEN_REPLY_COMPOSER: String = "open_reply_composer"
    fun start(context: Any?, post: Post, openReplyComposer: Boolean = false) {}
    fun start(context: Any?, postId: String, openReplyComposer: Boolean = false) {}
    fun startForReplyToComment(
        context: Any?,
        postId: String,
        commentId: Long,
        authorName: String,
        content: String,
        avatar: Any? = null,
    ) {
    }
}
