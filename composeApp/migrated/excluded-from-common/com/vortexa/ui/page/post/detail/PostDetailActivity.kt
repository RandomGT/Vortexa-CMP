package com.vortexa.ui.page.post.detail

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import com.vortexa.model.Post
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.theme.BaseTheme

/**
 * 帖子详情页 Activity
 * 从 RecommendView 点击 Post Item 进入，通过 postId 调用 /v/api/home/posts/{postId} 加载详情
 */
class PostDetailActivity : BaseActivity() {

    @Composable
    override fun ContentPage() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val postId = intent.getStringExtra(EXTRA_POST_ID) ?: ""
        val editPayload = parseEditPayload(intent)
        val replyComposerHint = parseReplyComposerHint(intent)
        val openReplyComposerOnLoad = intent.getBooleanExtra(EXTRA_OPEN_REPLY_COMPOSER, false)
        BaseTheme(belowStatusBar = true) {
            PostDetailView(
                postId = postId,
                editPayload = editPayload,
                replyComposerHint = replyComposerHint,
                openReplyComposerOnLoad = openReplyComposerOnLoad && replyComposerHint == null
            )
        }
    }

    companion object {
        private const val EXTRA_POST_ID = "extra_post_id"
        private const val EXTRA_EDIT_TITLE = "extra_edit_title"
        private const val EXTRA_EDIT_CONTENT = "extra_edit_content"
        private const val EXTRA_EDIT_IMAGES = "extra_edit_images"
        private const val EXTRA_EDIT_VIDEOS = "extra_edit_videos"
        private const val EXTRA_REPLY_COMMENT_ID = "extra_reply_comment_id"
        private const val EXTRA_REPLY_AUTHOR_NAME = "extra_reply_author_name"
        private const val EXTRA_REPLY_COMMENT_SNIPPET = "extra_reply_comment_snippet"
        private const val EXTRA_REPLY_AUTHOR_AVATAR = "extra_reply_author_avatar"
        private const val EXTRA_OPEN_REPLY_COMPOSER = "extra_open_reply_composer"

        /** 启动帖子详情页，传入帖子对象，取其 id 供接口请求 */
        fun start(context: Context, post: Post, openReplyComposer: Boolean = false) {
            start(context, post.id, openReplyComposer)
        }

        /** 启动帖子详情页，仅传入 postId 字符串（如从互动列表跳转） */
        fun start(context: Context, postId: String, openReplyComposer: Boolean = false) {
            context.startActivity(Intent(context, PostDetailActivity::class.java).apply {
                putExtra(EXTRA_POST_ID, postId)
                putExtra(EXTRA_OPEN_REPLY_COMPOSER, openReplyComposer)
            })
        }

        /**
         * 进入帖子详情并自动聚焦评论框、弹起键盘；可选带上被回复的一级评论信息（与「回复」指示条一致）。
         */
        fun startForReplyToComment(
            context: Context,
            postId: String,
            commentId: Long,
            authorName: String,
            commentContent: String,
            authorAvatar: String? = null
        ) {
            context.startActivity(Intent(context, PostDetailActivity::class.java).apply {
                putExtra(EXTRA_POST_ID, postId)
                putExtra(EXTRA_REPLY_COMMENT_ID, commentId)
                putExtra(EXTRA_REPLY_AUTHOR_NAME, authorName)
                putExtra(EXTRA_REPLY_COMMENT_SNIPPET, commentContent)
                putExtra(EXTRA_REPLY_AUTHOR_AVATAR, authorAvatar)
            })
        }

        /**
         * 启动帖子详情页编辑态，携带标题、正文、图片和视频资源。
         * @param context 上下文
         * @param postId 帖子 ID
         * @param title 编辑标题
         * @param content 编辑正文
         * @param imageResources 图片资源（支持本地 Uri 字符串或远程 URL）
         * @param videoResources 视频资源（支持本地 Uri 字符串或远程 URL）
         */
        fun startForEdit(
            context: Context,
            postId: String,
            title: String,
            content: String,
            imageResources: List<String> = emptyList(),
            videoResources: List<String> = emptyList()
        ) {
            context.startActivity(Intent(context, PostDetailActivity::class.java).apply {
                putExtra(EXTRA_POST_ID, postId)
                putExtra(EXTRA_EDIT_TITLE, title)
                putExtra(EXTRA_EDIT_CONTENT, content)
                putStringArrayListExtra(EXTRA_EDIT_IMAGES, ArrayList(imageResources))
                putStringArrayListExtra(EXTRA_EDIT_VIDEOS, ArrayList(videoResources))
            })
        }

        /**
         * 从 Intent 解析编辑态参数。
         * @param intent 页面启动 Intent
         * @return 当存在任一编辑字段时返回编辑入参，否则返回 null
         */
        private fun parseEditPayload(intent: Intent): PostDetailEditPayload? {
            val title = intent.getStringExtra(EXTRA_EDIT_TITLE).orEmpty()
            val content = intent.getStringExtra(EXTRA_EDIT_CONTENT).orEmpty()
            val images = intent.getStringArrayListExtra(EXTRA_EDIT_IMAGES)?.toList().orEmpty()
            val videos = intent.getStringArrayListExtra(EXTRA_EDIT_VIDEOS)?.toList().orEmpty()
            val hasPayload = title.isNotBlank() || content.isNotBlank() || images.isNotEmpty() || videos.isNotEmpty()
            if (!hasPayload) return null
            Log.i(
                "PostDetailActivity",
                "parseEditPayload: titleLength=${title.length}, contentLength=${content.length}, " +
                    "imageCount=${images.size}, videoCount=${videos.size}"
            )
            return PostDetailEditPayload(
                title = title,
                content = content,
                imageResources = images,
                videoResources = videos
            )
        }

        private fun parseReplyComposerHint(intent: Intent): PostDetailReplyComposerHint? {
            val commentId = intent.getLongExtra(EXTRA_REPLY_COMMENT_ID, -1L)
            if (commentId < 0L) return null
            return PostDetailReplyComposerHint(
                commentId = commentId,
                authorName = intent.getStringExtra(EXTRA_REPLY_AUTHOR_NAME).orEmpty(),
                commentSnippet = intent.getStringExtra(EXTRA_REPLY_COMMENT_SNIPPET).orEmpty(),
                authorAvatar = intent.getStringExtra(EXTRA_REPLY_AUTHOR_AVATAR)
            )
        }
    }
}

/**
 * 从外链进入详情页时预置「回复某条评论」：`ReplyIndicatorBar` 与发送时的 parentCommentId 均依赖其中 [commentId]。
 */
data class PostDetailReplyComposerHint(
    val commentId: Long,
    val authorName: String,
    val commentSnippet: String,
    val authorAvatar: String?
)

/**
 * 帖子详情编辑态入参。
 * @param title 编辑标题
 * @param content 编辑正文
 * @param imageResources 图片资源（本地 Uri 或远程 URL）
 * @param videoResources 视频资源（本地 Uri 或远程 URL）
 */
data class PostDetailEditPayload(
    val title: String,
    val content: String,
    val imageResources: List<String>,
    val videoResources: List<String>
)
