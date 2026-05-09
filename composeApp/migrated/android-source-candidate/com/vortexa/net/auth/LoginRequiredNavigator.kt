package com.vortexa.net.auth

import android.app.Application
import android.content.Intent
import android.util.Log
import com.vortexa.ui.page.login.LoginActivity

object LoginRequiredNavigator {

    private const val TAG = "LoginRedirect"

    fun openInlineLogin(app: Application) {
        Log.w(TAG, "跳转登录页: LoginRequiredNavigator -> LoginActivity(EXTRA_INLINE_AUTH), CLEAR_TOP|NEW_TASK")
        val intent = Intent(app, LoginActivity::class.java).apply {
            putExtra(LoginActivity.EXTRA_INLINE_AUTH, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        app.startActivity(intent)
    }
}
