package com.vortexa.util

/** 接口请求前：手机号须为 11 位数字（自动 trim） */
fun isElevenDigitMobile(phone: String): Boolean {
    val p = phone.trim()
    return p.length == 11 && p.all { it.isDigit() }
}

const val PHONE_ELEVEN_DIGIT_TOAST = "请输入11位手机号"
