package com.vortexa.router

import com.vortexa.util.sp.SpHelper

/**
 * 未登录时拦截 Scheme 后暂存，登录/注册成功后再 [consume] 并交给 [AppSchemeRouter.open]。
 */
object PendingRouteStore {

    private const val KEY_PENDING_URI = "pending_app_scheme_uri"

    fun save(raw: String) {
        if (raw.isNotBlank()) {
            SpHelper.putString(KEY_PENDING_URI, raw)
        }
    }

    fun peek(): String = SpHelper.getString(KEY_PENDING_URI)

    fun consume(): String? {
        val v = SpHelper.getString(KEY_PENDING_URI)
        if (v.isNotEmpty()) {
            SpHelper.remove(KEY_PENDING_URI)
            return v
        }
        return null
    }

    fun clear() {
        SpHelper.remove(KEY_PENDING_URI)
    }
}
