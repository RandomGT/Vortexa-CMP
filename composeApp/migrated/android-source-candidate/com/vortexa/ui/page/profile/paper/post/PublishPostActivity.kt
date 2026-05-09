package com.vortexa.ui.page.profile.paper.post

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.theme.BaseTheme

/**
 * 发布/编辑稿件页。
 * 编辑时通过 intent 传入 postId。
 */
class PublishPostActivity : BaseActivity() {

    @Composable
    override fun ContentPage() {
        BaseTheme(belowStatusBar = true, aboveNavigationBar = true) {
            PublishPostView()
        }
    }

    companion object {
        private const val EXTRA_POST_ID = "extra_post_id"

        /**
         * 启动编辑稿件页。
         * @param context 上下文
         * @param postId 稿件 ID，0 表示新建
         */
        fun start(context: Context, postId: Long = 0L) {
            context.startActivity(Intent(context, PublishPostActivity::class.java).apply {
                putExtra(EXTRA_POST_ID, postId)
            })
        }
    }
}