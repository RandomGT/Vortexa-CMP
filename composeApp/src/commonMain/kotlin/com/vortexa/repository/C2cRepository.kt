package com.vortexa.repository

import com.vortexa.model.RtcChannelUserProfile
import com.vortexa.model.TeacherListItem
import com.vortexa.model.TeacherListResponse
import com.vortexa.net.ApiClient
import com.vortexa.net.ApiException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

class C2cRepository(
    private val client: ApiClient = ApiClient
) {
    private val userRepository by lazy { UserRepository() }

    suspend fun getTeacherList(
        tags: String? = null,
        minPrice: String? = null,
        maxPrice: String? = null,
        pageNum: Int = 1,
        pageSize: Int = 5,
    ): Result<TeacherListResponse> =
        runCatching {
            val response = client.getJson(
                "v/api/c2c/teacher/list",
                mapOf(
                    "tags" to tags,
                    "minPrice" to minPrice,
                    "maxPrice" to maxPrice,
                    "pageNum" to pageNum,
                    "pageSize" to pageSize
                )
            )
            val data = response.data as? JsonObject ?: throw ApiException(-1, "Response data is null")
            TeacherListResponse(
                pageNum = data.int("pageNum") ?: pageNum,
                pageSize = data.int("pageSize") ?: pageSize,
                total = data.int("total") ?: data.array("list").size,
                list = data.array("list").map { item ->
                    val obj = item as? JsonObject ?: JsonObject(emptyMap())
                    TeacherListItem(
                        teacherId = obj.long("teacherId") ?: obj.long("userId") ?: 0L,
                        avatar = obj.string("avatar"),
                        teacherAvatar = obj.string("teacherAvatar"),
                        nickName = obj.string("nickName") ?: obj.string("nickname") ?: "",
                        price = obj.string("price") ?: obj.numberString("price") ?: "0",
                        score = obj.string("score") ?: obj.numberString("score") ?: "0",
                        tagList = obj.stringList("tagList") ?: obj.stringList("tags"),
                        priceUnit = obj.string("priceUnit")
                    )
                }
            )
        }

    suspend fun getC2cToken(channelName: String): Result<String> = runCatching {
        val response = client.getJson(
            "v/api/c2c/token",
            mapOf("channelName" to channelName)
        )
        response.dataString() ?: throw ApiException(-1, "Token 为空")
    }

    suspend fun getRtcChannelUserProfile(agoraUid: Int): Result<RtcChannelUserProfile> {
        return userRepository.getUserProfile(agoraUid.toLong()).map { response ->
            val info = response.userInfo
            RtcChannelUserProfile(
                agoraUid = agoraUid,
                nickName = info.nickname,
                avatar = info.avatar,
                role = null,
                teacherId = info.teacherId
            )
        }
    }
}

private fun JsonObject.array(key: String): List<kotlinx.serialization.json.JsonElement> =
    (this[key] as? JsonArray)?.toList().orEmpty()

private fun JsonObject.string(key: String): String? =
    (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content

private fun JsonObject.numberString(key: String): String? =
    (this[key] as? JsonPrimitive)?.content

private fun JsonObject.int(key: String): Int? =
    (this[key] as? JsonPrimitive)?.intOrNull

private fun JsonObject.long(key: String): Long? =
    (this[key] as? JsonPrimitive)?.longOrNull

private fun JsonObject.stringList(key: String): List<String>? =
    (this[key] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.content }

private fun com.vortexa.net.ApiResponse.dataString(): String? =
    (data as? JsonPrimitive)?.contentOrNull
        ?: (data as? JsonObject)?.let {
            (it["token"] as? JsonPrimitive)?.contentOrNull
                ?: (it["data"] as? JsonPrimitive)?.contentOrNull
                ?: (it["value"] as? JsonPrimitive)?.contentOrNull
        }
