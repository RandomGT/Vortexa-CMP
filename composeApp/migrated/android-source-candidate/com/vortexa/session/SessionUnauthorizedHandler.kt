package com.vortexa.session

import android.app.Application
import android.content.Intent
import android.util.Log
import com.vortexa.config.TokenConfig
import com.vortexa.config.UserConfig
import com.vortexa.ui.page.login.LoginActivity

/**
 * 业务码 401（未登录/登录失效）时：清本地会话并回到登录页。
 * 仅在本地曾持有 token 时处理，避免登录/注册等无 token 请求的 401 误杀会话。
 */
object SessionUnauthorizedHandler {

    private const val TAG = "LoginRedirect"

    fun resetGate() {
        AuthNavGate.reset()
    }

    fun handle(app: Application) {
        if (TokenConfig.getToken().isEmpty()) {
            Log.d(TAG, "SessionUnauthorizedHandler: 本地无 Token，忽略 401 登出跳转")
            return
        }
        if (!AuthNavGate.tryEnterUnauthorizedFlow()) {
            Log.d(TAG, "SessionUnauthorizedHandler: 已在 401 登出流程中，跳过重复跳转")
            return
        }
        Log.w(
            TAG,
            "跳转登录页: SessionUnauthorizedHandler（HTTP/业务 401，会话失效）-> LoginActivity CLEAR_TASK"
        )
        TokenConfig.clearToken()
        UserConfig.clear()
        val intent = Intent(app, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        app.startActivity(intent)
    }
}
