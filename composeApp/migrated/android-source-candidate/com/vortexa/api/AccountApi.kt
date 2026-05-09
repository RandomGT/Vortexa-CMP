package com.vortexa.api

import com.vortexa.lib_net.model.ApiResponse
import com.vortexa.model.AuthRequest
import com.vortexa.model.AuthResponse
import com.vortexa.model.ResetPasswordRequest
import com.vortexa.model.SmsCodeRequest
import com.vortexa.model.SmsVerifyData
import com.vortexa.model.SmsVerifyRequest
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 账号相关接口
 *
 * @author LuXin
 */
interface AccountApi {

    /**
     * 用户登录/注册
     *
     * @param request 认证请求体
     * @return ApiResponse<AuthResponse>
     */
    @POST("v/api/account/auth")
    suspend fun auth(@Body request: AuthRequest): ApiResponse<AuthResponse>

    /**
     * 获取短信验证码
     *
     * @param request 手机号
     * @return ApiResponse 统一响应格式
     */
    @POST("v/api/account/sms/code")
    suspend fun getSmsCode(@Body request: SmsCodeRequest): ApiResponse<Unit?>

    /**
     * 校验短信验证码（成功后服务端 Redis 记录，后续重置密码/注册等依赖该状态）
     */
    @POST("v/api/account/sms/verify")
    suspend fun verifySms(@Body request: SmsVerifyRequest): ApiResponse<SmsVerifyData?>

    /**
     * 忘记密码：重置密码
     *
     * @param request 手机号、短信验证码、新密码
     * @return ApiResponse 成功时 data 为空对象
     */
    @POST("v/api/account/reset")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): ApiResponse<Unit?>
}
