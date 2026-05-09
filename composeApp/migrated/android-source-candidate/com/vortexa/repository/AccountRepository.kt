package com.vortexa.repository

import android.util.Log
import com.vortexa.api.AccountApi
import com.vortexa.lib_net.client.RetrofitClient
import com.vortexa.lib_net.exception.ApiException
import com.vortexa.model.AuthRequest
import com.vortexa.model.AuthResponse
import com.vortexa.model.ResetPasswordRequest
import com.vortexa.model.SmsCodeRequest
import com.vortexa.model.SmsVerifyRequest

/**
 * 账号相关数据仓库，负责短信验证码等接口调用
 *
 * @author LuXin
 */
class AccountRepository {

    private val api: AccountApi by lazy {
        RetrofitClient.createService()
    }

    /**
     * 用户登录/注册
     *
     * @param request 认证请求体
     * @return Result<AuthResponse>
     */
    suspend fun auth(request: AuthRequest): Result<AuthResponse> = runCatching {
        Log.d(TAG, "auth: type=${request.authType}, phone=${request.phone}, user=${request.userName}")
        val response = api.auth(request)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    /**
     * 获取短信验证码
     *
     * @param phone 手机号
     * @return Result 成功返回 Unit，失败返回异常
     */
    suspend fun getSmsCode(phone: String): Result<Unit> = runCatching {
        Log.d(TAG, "getSmsCode: phone=$phone")
        val response = api.getSmsCode(SmsCodeRequest(phone))
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        Log.i(TAG, "getSmsCode: 验证码已发送")
        Unit
    }

    /**
     * 校验短信验证码
     *
     * @param phone 手机号
     * @param smsCode 短信验证码
     * @return Result 成功表示服务端已记录校验通过（可与后续重置/注册流程配合）
     */
    suspend fun verifySmsCode(phone: String, smsCode: String): Result<Unit> = runCatching {
        Log.d(TAG, "verifySmsCode: phone=$phone")
        val response = api.verifySms(SmsVerifyRequest(phone, smsCode))
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        Log.i(TAG, "verifySmsCode: ${response.data?.msg ?: "验证成功"}")
        Unit
    }

    /**
     * 忘记密码：重置密码
     *
     * @param request 手机号、短信验证码、新密码
     * @return Result 成功返回 Unit，失败抛出 ApiException
     */
    suspend fun resetPassword(request: ResetPasswordRequest): Result<Unit> = runCatching {
        Log.d(TAG, "resetPassword: phone=${request.phone}")
        val response = api.resetPassword(request)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        Log.i(TAG, "resetPassword: 密码已重置")
        Unit
    }

    companion object {
        private const val TAG = "AccountRepository"
    }
}
