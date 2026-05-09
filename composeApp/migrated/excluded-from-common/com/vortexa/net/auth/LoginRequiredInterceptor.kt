package com.vortexa.net.auth

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.vortexa.config.TokenConfig
import com.vortexa.lib_net.LibNet
import com.vortexa.lib_net.exception.LoginRequiredException
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 无 token 时拦截 [LoginProtectedPathMatcher] 命中的请求，触发 [LibNet.onLoginRequired] 并抛出 [LoginRequiredException]。
 */
class LoginRequiredInterceptor : Interceptor {

    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "LoginRequiredInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (TokenConfig.getToken().isNotEmpty()) {
            return chain.proceed(request)
        }
        val path = request.url.encodedPath
        val method = request.method
        if (!LoginProtectedPathMatcher.requiresLogin(method, path)) {
            return chain.proceed(request)
        }
        Log.w(
            TAG,
            "跳转登录页: 无 Token 且命中受保护接口，将打开内联登录 | method=$method path=$path url=${request.url}"
        )
        val loginCb = LibNet.onLoginRequired
        if (loginCb != null) {
            mainHandler.post {
                try {
                    loginCb.invoke()
                } catch (e: Exception) {
                    Log.e(TAG, "onLoginRequired failed", e)
                }
            }
        } else {
            Log.w(TAG, "跳转登录页: LibNet.onLoginRequired 未注册，仅中止请求")
        }
        throw LoginRequiredException(method, path)
    }
}
