package com.vortexa.net

import com.vortexa.model.AuthRequest
import com.vortexa.model.AuthResponse
import com.vortexa.model.ResetPasswordRequest
import com.vortexa.model.SmsCodeRequest
import com.vortexa.model.SmsVerifyData
import com.vortexa.model.SmsVerifyRequest
import com.vortexa.model.UserInfo
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AccountApi(
    private val client: ApiClient = ApiClient
) {
    suspend fun auth(request: AuthRequest): AuthResponse {
        val response = client.postJson(PATH_AUTH, request.toJson())
        val data = response.data as? JsonObject ?: throw ApiException(-1, "Response data is null")
        return data.toAuthResponse()
    }

    suspend fun getSmsCode(request: SmsCodeRequest) {
        client.postJson(PATH_SMS_CODE, request.toJson())
    }

    suspend fun verifySms(request: SmsVerifyRequest): SmsVerifyData {
        val response = client.postJson(PATH_SMS_VERIFY, request.toJson())
        return (response.data as? JsonObject)?.toSmsVerifyData() ?: SmsVerifyData()
    }

    suspend fun resetPassword(request: ResetPasswordRequest) {
        client.postJson(PATH_RESET_PASSWORD, request.toJson())
    }

    companion object {
        const val PATH_AUTH = "v/api/account/auth"
        const val PATH_SMS_CODE = "v/api/account/sms/code"
        const val PATH_SMS_VERIFY = "v/api/account/sms/verify"
        const val PATH_RESET_PASSWORD = "v/api/account/reset"
    }
}

private fun AuthRequest.toJson(): JsonObject = buildJsonObject {
    put("authType", authType)
    putIfNotNull("userName", userName)
    putIfNotNull("phone", phone)
    put("password", password)
    putIfNotNull("smsCode", smsCode)
    putIfNotNull("nickname", nickname)
    putIfNotNull("verifyCode", verifyCode)
}

private fun SmsCodeRequest.toJson(): JsonObject = buildJsonObject {
    put("phone", phone)
}

private fun SmsVerifyRequest.toJson(): JsonObject = buildJsonObject {
    put("phone", phone)
    put("smsCode", smsCode)
}

private fun ResetPasswordRequest.toJson(): JsonObject = buildJsonObject {
    put("phone", phone)
    put("smsCode", smsCode)
    put("newPassword", newPassword)
}

private fun JsonObject.toAuthResponse(): AuthResponse {
    val token = stringValue("token") ?: throw ApiException(-1, "Token is null")
    val userInfo = (this["userInfoLogin"] as? JsonObject)?.toUserInfo()
        ?: throw ApiException(-1, "User info is null")
    return AuthResponse(token = token, userInfoLogin = userInfo)
}

private fun JsonObject.toUserInfo(): UserInfo = UserInfo(
    id = longValue("id") ?: 0L,
    avatar = stringValue("avatar"),
    nickname = stringValue("nickname"),
    role = stringValue("role")
)

private fun JsonObject.toSmsVerifyData(): SmsVerifyData = SmsVerifyData(
    msg = stringValue("msg")
)

private fun JsonObject.longValue(key: String): Long? =
    (this[key] as? JsonPrimitive)?.content?.toLongOrNull()

private fun JsonObjectBuilder.putIfNotNull(key: String, value: String?) {
    if (value != null) {
        put(key, value)
    }
}
