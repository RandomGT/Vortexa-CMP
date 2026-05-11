package com.vortexa.ui.page.post.list

import com.vortexa.navigation.AppRoute
import com.vortexa.navigation.NavigationRouteBridge

object HotPostListActivity {
    fun start(context: Any?) {
        NavigationRouteBridge.navigate(AppRoute.HotPostList)
    }
}
