package com.vortexa.ui.page.profile.collection

import androidx.compose.runtime.Composable
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.theme.BaseTheme

/**
 * 我的收藏页 Activity。
 * 展示用户收藏的帖子列表，支持进入详情、点赞、取消收藏。
 *
 * @author LuXin
 */
class CollectionActivity : BaseActivity() {

    @Composable
    override fun ContentPage() {
        BaseTheme(aboveNavigationBar = true, belowStatusBar = true) {
            CollectionView(
                onBackClick = { finish() }
            )
        }
    }
}
