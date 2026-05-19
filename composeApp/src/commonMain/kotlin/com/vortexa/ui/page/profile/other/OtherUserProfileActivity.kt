package com.vortexa.ui.page.profile.other

import com.vortexa.config.UserConfig

/**
 * 他人主页导航桥。
 *
 * commonMain 里不直接创建 Android Activity/Intent；主导航接入时注册回调即可。
 * 现有帖子、评论组件仍可继续调用 startIfNotSelf。
 */
object OtherUserProfileActivity {
    private data class NavigationCallbacks(
        val openOtherUserProfile: (Long) -> Unit,
        val openSelfProfile: () -> Unit
    )

    private val navigationCallbacks = mutableListOf<NavigationCallbacks>()

    fun bindNavigation(
        onOpenOtherUserProfile: (Long) -> Unit,
        onOpenSelfProfile: () -> Unit
    ): Any {
        val callbacks = NavigationCallbacks(onOpenOtherUserProfile, onOpenSelfProfile)
        navigationCallbacks += callbacks
        return callbacks
    }

    fun clearNavigationCallbacks(token: Any? = null) {
        if (token == null) {
            navigationCallbacks.clear()
            return
        }
        (token as? NavigationCallbacks)?.let { navigationCallbacks.remove(it) }
    }

    fun start(context: Any?, userId: Long) {
        openUserProfile(userId)
    }

    fun startIfNotSelf(context: Any?, userId: Long) {
        openUserProfile(userId)
    }

    private fun openUserProfile(userId: Long) {
        if (userId <= 0L) return
        val callbacks = navigationCallbacks.lastOrNull() ?: return
        if (userId == UserConfig.getUserId()) {
            callbacks.openSelfProfile()
            return
        }
        callbacks.openOtherUserProfile(userId)
    }
}
