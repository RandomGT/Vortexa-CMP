package com.vortexa.util

/** 与注册/重置密码接口一致的服务端密码复杂度 */
val AUTH_PASSWORD_REGEX =
    Regex("""^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-={}\[\]:'"|,.<>/?]).{8,}$""")

const val AUTH_PASSWORD_RULE_TIP = "密码需至少 8 位，且包含大写字母、小写字母、数字和特殊字符"
