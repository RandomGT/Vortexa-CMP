package com.vortexa.repository

import com.vortexa.net.AccountApi
import com.vortexa.model.AuthRequest
import com.vortexa.model.AuthResponse
import com.vortexa.model.ResetPasswordRequest
import com.vortexa.model.SmsCodeRequest
import com.vortexa.model.SmsVerifyRequest

class AccountRepository {
    private val api: AccountApi = AccountApi()

    suspend fun auth(request: AuthRequest): Result<AuthResponse> = runCatching {
        api.auth(request)
    }

    suspend fun getSmsCode(phone: String): Result<Unit> = runCatching {
        api.getSmsCode(SmsCodeRequest(phone))
    }

    suspend fun verifySmsCode(phone: String, code: String): Result<Unit> = runCatching {
        api.verifySms(SmsVerifyRequest(phone, code))
        Unit
    }

    suspend fun resetPassword(request: ResetPasswordRequest): Result<Unit> = runCatching {
        api.resetPassword(request)
    }
}
