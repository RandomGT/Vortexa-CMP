package com.vortexa.ui.page.post.list

import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.theme.BaseTheme

/**
 * 热帖列表页 Activity，展示标题「热帖」、返回按钮及每页 10 条的帖子列表。
 */
class HotPostListActivity : BaseActivity() {

    @Composable
    override fun ContentPage() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        BaseTheme(belowStatusBar = true) {
            HotPostListView(onBackClick = { finish() })
        }
    }
}
