package com.vortexa.ui.page.post.create

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.theme.BaseTheme

/**
 * 发布贴文页 Activity。
 * 新建：POST /v/api/home/post/insert；编辑（带 postId）：PUT /v/api/user/posts/update/{postId}。
 */
class PostCreateActivity : BaseActivity() {

    @Composable
    override fun ContentPage() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        BaseTheme(belowStatusBar = true) {
            PostCreateView()
        }
    }

    companion object {
        private const val TAG = "PostCreateActivity"
        private const val EXTRA_EDIT_POST_ID = "extra_edit_post_id"
        private const val EXTRA_EDIT_TITLE = "extra_edit_title"
        private const val EXTRA_EDIT_CONTENT = "extra_edit_content"
        private const val EXTRA_EDIT_BOARD = "extra_edit_board"
        private const val EXTRA_EDIT_IMAGES = "extra_edit_images"
        private const val EXTRA_EDIT_VIDEOS = "extra_edit_videos"

        /** 启动发布贴文页 */
        fun start(context: Context) {
            context.startActivity(Intent(context, PostCreateActivity::class.java))
        }

        /**
         * 从帖子详情进入发帖页编辑草稿（标题、正文、分区、媒体 URL）。
         */
        fun startForEdit(
            context: Context,
            postId: String,
            title: String,
            content: String,
            board: String? = null,
            imageResources: List<String> = emptyList(),
            videoResources: List<String> = emptyList()
        ) {
            context.startActivity(Intent(context, PostCreateActivity::class.java).apply {
                putExtra(EXTRA_EDIT_POST_ID, postId)
                putExtra(EXTRA_EDIT_TITLE, title)
                putExtra(EXTRA_EDIT_CONTENT, content)
                putExtra(EXTRA_EDIT_BOARD, board)
                putStringArrayListExtra(EXTRA_EDIT_IMAGES, ArrayList(imageResources))
                putStringArrayListExtra(EXTRA_EDIT_VIDEOS, ArrayList(videoResources))
            })
        }

        /** 若 Intent 含编辑帖 id 则解析为 [PostCreateEditArgs]，否则 null */
        fun parseEditArgs(intent: Intent): PostCreateEditArgs? {
            val postId = intent.getStringExtra(EXTRA_EDIT_POST_ID)?.trim().orEmpty()
            if (postId.isEmpty()) return null
            val title = intent.getStringExtra(EXTRA_EDIT_TITLE).orEmpty()
            val content = intent.getStringExtra(EXTRA_EDIT_CONTENT).orEmpty()
            val board = intent.getStringExtra(EXTRA_EDIT_BOARD)?.takeIf { it.isNotBlank() }
            val images = intent.getStringArrayListExtra(EXTRA_EDIT_IMAGES)?.toList().orEmpty()
            val videos = intent.getStringArrayListExtra(EXTRA_EDIT_VIDEOS)?.toList().orEmpty()
            Log.i(TAG, "parseEditArgs: postId=$postId, images=${images.size}, videos=${videos.size}")
            return PostCreateEditArgs(
                postId = postId,
                title = title,
                content = content,
                board = board,
                imageResources = images,
                videoResources = videos
            )
        }
    }
}

/**
 * 发帖页编辑入参（由详情「更多-编辑」传入）。
 */
data class PostCreateEditArgs(
    val postId: String,
    val title: String,
    val content: String,
    val board: String?,
    val imageResources: List<String>,
    val videoResources: List<String>
)
