package com.vortexa.model

/**
 * 认证请求体
 */
data class AuthRequest(
    /** 认证类型：LOGIN_PASSWORD (密码登录), REGISTER (注册) */
    val authType: String,
    /** 账号（登录用） */
    val userName: String? = null,
    /** 手机号（注册用） */
    val phone: String? = null,
    /** 密码 */
    val password: String,
    /** 短信验证码（注册用） */
    val smsCode: String? = null,
    /** 昵称（注册用） */
    val nickname: String? = null,
    /** 图形验证码（登录用） */
    val verifyCode: String? = null
)

/**
 * 认证响应体
 */
data class AuthResponse(
    val token: String,
    val userInfoLogin: UserInfo
)

data class UserInfo(
    val id: Long,
    val avatar: String?,
    val nickname: String?,
    val role: String?
)

/**
 * 获取短信验证码请求体
 */
data class SmsCodeRequest(
    /** 手机号 */
    val phone: String
)

/**
 * 短信验证码校验请求体（服务端 Redis 记录校验成功，约 5 分钟内有效）
 */
data class SmsVerifyRequest(
    val phone: String,
    val smsCode: String
)

data class SmsVerifyData(
    val msg: String? = null
)

/**
 * 忘记密码重置请求体
 */
data class ResetPasswordRequest(
    /** 手机号 */
    val phone: String,
    /** 短信验证码 */
    val smsCode: String,
    /** 新密码 */
    val newPassword: String
)
