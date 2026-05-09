package com.vortexa.config

import com.vortexa.session.AuthNavGate
import com.vortexa.util.sp.SpHelper

/**
 *  desc : Http Token 配置
 *
 *
 *  @author LuXin
 *  @createTime 2026/2/28
 */
object TokenConfig {

    private var token: String = ""
    private const val TOKEN_KEY = "token"

    fun updateToken(token: String) {
        this.token = token
        SpHelper.putString(TOKEN_KEY, token)
        if (token.isNotEmpty()) {
            AuthNavGate.reset()
        }
    }


    fun getToken(): String {
        //从SharedPreference中获取
        if (token.isEmpty()) {
            token = SpHelper.getString(TOKEN_KEY)
        }
        return token
    }

    fun clearToken() {
        token = ""
        SpHelper.remove(TOKEN_KEY)
    }
}