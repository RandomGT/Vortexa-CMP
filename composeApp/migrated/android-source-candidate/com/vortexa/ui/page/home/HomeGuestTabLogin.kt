package com.vortexa.ui.page.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.vortexa.config.TokenConfig
import com.vortexa.ui.page.login.LoginActivity

/**
 * 未登录时首页底部「消息 / 关注 / 我的」不切换 Tab，直接进登录页（内联模式）。
 */
object HomeGuestTabLogin {

    private val tabsRequiringLoginWhenGuest = setOf(1, 3, 4)

    /**
     * @return true 表示已拉起登录，调用方须**不要**再执行切 Tab。
     */
    fun openGuestLoginInsteadOfTab(context: Context, tabIndex: Int): Boolean {
        if (TokenConfig.getToken().isNotEmpty()) return false
        if (tabIndex !in tabsRequiringLoginWhenGuest) return false
        val intent = Intent(context, LoginActivity::class.java).apply {
            putExtra(LoginActivity.EXTRA_INLINE_AUTH, true)
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent)
        return true
    }
}
