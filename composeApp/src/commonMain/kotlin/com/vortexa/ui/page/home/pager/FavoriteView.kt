package com.vortexa.ui.page.home.pager

import androidx.compose.runtime.Composable
import com.vortexa.ui.page.home.pager.follow.FollowView

/**
 *  desc : 收藏/关注 Tab 页，展示关注流（FollowView）
 *
 *  @author LuXin
 *  @createTime 2026/1/19
 */
@Composable
fun FavoriteView(id: Int, isSelected: Boolean = true) {
    FollowView(isSelected = isSelected)
}