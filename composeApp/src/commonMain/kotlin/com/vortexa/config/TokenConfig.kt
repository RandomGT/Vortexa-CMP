package com.vortexa.config

import com.vortexa.session.AuthNavGate
import com.vortexa.platform.authSessionGetString
import com.vortexa.platform.authSessionPutString
import com.vortexa.platform.authSessionRemove

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
        authSessionPutString(TOKEN_KEY, token)
        if (token.isNotEmpty()) {
            AuthNavGate.reset()
        }
    }


    fun getToken(): String {
        //从SharedPreference中获取
        if (token.isEmpty()) {
            token = authSessionGetString(TOKEN_KEY)
        }
        return token
    }

    fun clearToken() {
        token = ""
        authSessionRemove(TOKEN_KEY)
    }
}
