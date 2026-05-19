package com.vortexa.ui.page.teach.helper

import com.vortexa.model.ReserveClassroomDetail
import com.vortexa.model.ReserveDetail
import com.vortexa.model.ReserveListItem
import com.vortexa.model.TeacherDetailBaseInfo
import com.vortexa.model.TeacherDetailCourse
import com.vortexa.model.TeacherDetailEvaluate
import com.vortexa.model.TeacherDetailResponse
import com.vortexa.model.TeacherReserveReceipt
import com.vortexa.model.TeacherReserveTimeItem
import com.vortexa.net.ApiClient
import com.vortexa.net.ApiException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

class TeachingC2cRepository(
    private val client: ApiClient = ApiClient
) {
    suspend fun getTeacherDetail(teacherId: Long): Result<TeacherDetailResponse> = runCatching {
        val data = client.getJson(
            "v/api/c2c/teacher/detail",
            mapOf("teacherId" to teacherId)
        ).dataObject()
        data.toTeacherDetailResponse()
    }

    suspend fun getTeacherReserveTime(
        teacherId: Long,
        reserveDate: String
    ): Result<List<TeacherReserveTimeItem>> = runCatching {
        client.getJson(
            "v/api/c2c/teacher/reserve/time",
            mapOf("teacherId" to teacherId, "reserveDate" to reserveDate)
        ).dataArray().map { it.asObject().toTeacherReserveTimeItem() }
    }

    suspend fun reserve(
        teacherId: Long,
        reserveDate: String,
        reserveHour: String,
        userId: Long
    ): Result<TeacherReserveReceipt> = runCatching {
        client.postJson(
            "v/api/c2c/teacher/reserve",
            buildJsonObject {
                put("teacherId", teacherId.toInt())
                put("reserveDate", reserveDate)
                put("reserveHour", reserveHour)
                put("userId", userId)
            }
        ).dataObject().toTeacherReserveReceipt()
    }

    suspend fun getReserveList(type: Int, status: String? = null): Result<List<ReserveListItem>> = runCatching {
        client.getJson(
            "v/api/c2c/teacher/reserve/list",
            mapOf("type" to type, "status" to status?.trim()?.takeIf { it.isNotEmpty() })
        ).dataArray().map { it.asObject().toReserveListItem() }
    }

    suspend fun getReserveDetail(reserveId: Int): Result<ReserveDetail> = runCatching {
        client.getJson(
            "v/api/c2c/teacher/reserve/detail",
            mapOf("reserveId" to reserveId)
        ).dataObject().toReserveDetail()
    }

    suspend fun getReserveClassroom(reserveId: Int): Result<ReserveClassroomDetail> = runCatching {
        val data = client.getJson(
            "v/api/c2c/teacher/reserve/classroom",
            mapOf("reserveId" to reserveId)
        ).data as? JsonObject ?: JsonObject(emptyMap())
        data.toReserveClassroomDetail()
    }

    suspend fun cancelReserve(reserveId: Int, reason: String): Result<Unit> = runCatching {
        client.postJson(
            "v/api/c2c/teacher/reserve/cancel",
            buildJsonObject {
                put("reserveId", reserveId)
                put("reason", reason)
            }
        )
    }

    suspend fun acceptReserve(reserveId: Int): Result<Unit> = runCatching {
        client.postJson(
            "v/api/c2c/teacher/reserve/accept",
            buildJsonObject { put("reserveId", reserveId) }
        )
    }

    suspend fun rejectReserve(reserveId: Int, reason: String? = null): Result<Unit> = runCatching {
        client.postJson(
            "v/api/c2c/teacher/reserve/reject",
            buildJsonObject {
                put("reserveId", reserveId)
                if (reason != null) put("reason", reason)
            }
        )
    }
}

private fun com.vortexa.net.ApiResponse.dataObject(): JsonObject =
    data as? JsonObject ?: throw ApiException(-1, "Response data is null")

private fun com.vortexa.net.ApiResponse.dataArray(): List<JsonElement> =
    (data as? JsonArray)?.toList().orEmpty()

private fun JsonElement.asObject(): JsonObject = this as? JsonObject ?: JsonObject(emptyMap())

private fun JsonObject.toTeacherDetailResponse(): TeacherDetailResponse {
    val base = obj("baseInfo").takeIf { it.isNotEmpty() } ?: this
    return TeacherDetailResponse(
        baseInfo = TeacherDetailBaseInfo(
            teacherId = base.long("teacherId") ?: 0L,
            userId = base.long("userId") ?: 0L,
            avatar = base.string("avatar") ?: base.string("userAvatar") ?: base.string("avatarUrl")
                ?: base.string("headImg") ?: base.string("headPortrait"),
            nickName = base.string("nickName") ?: base.string("nickname") ?: "",
            score = base.string("score") ?: base.numberString("score") ?: "0",
            introduction = base.string("introduction"),
            completedConsultations = base.int("completedConsultations") ?: 0,
            price = base.float("price") ?: 0f
        ),
        courses = array("courses").map { it.asObject().toTeacherDetailCourse() },
        evaluates = array("evaluates").map { it.asObject().toTeacherDetailEvaluate() }
    )
}

private fun JsonObject.toTeacherDetailCourse(): TeacherDetailCourse =
    TeacherDetailCourse(
        id = long("id") ?: 0L,
        courseName = string("courseName") ?: ""
    )

private fun JsonObject.toTeacherDetailEvaluate(): TeacherDetailEvaluate =
    TeacherDetailEvaluate(
        id = long("id") ?: 0L,
        score = string("score") ?: numberString("score") ?: "0",
        title = string("title") ?: "",
        comment = string("comment") ?: "",
        createTimestamp = long("createTimestamp") ?: 0L
    )

private fun JsonObject.toTeacherReserveTimeItem(): TeacherReserveTimeItem =
    TeacherReserveTimeItem(
        reserveHour = string("reserveHour") ?: "",
        canReserve = boolean("canReserve") ?: false
    )

private fun JsonObject.toTeacherReserveReceipt(): TeacherReserveReceipt =
    TeacherReserveReceipt(
        reserveId = long("reserveId") ?: 0L,
        status = string("status") ?: "",
        reserveCreateTime = string("reserveCreateTime") ?: "",
        courseStartTime = string("courseStartTime") ?: "",
        hour = int("hour") ?: 0,
        orderPrice = string("orderPrice") ?: numberString("orderPrice"),
        payAmount = string("payAmount") ?: numberString("payAmount"),
        payType = string("payType")
    )

private fun JsonObject.toReserveListItem(): ReserveListItem =
    ReserveListItem(
        reserveId = long("reserveId") ?: 0L,
        status = string("status") ?: "",
        reserveCreateTime = string("reserveCreateTime") ?: "",
        courseStartTime = string("courseStartTime") ?: "",
        hour = int("hour") ?: 0,
        userId = long("userId") ?: 0L,
        teacherId = long("teacherId") ?: 0L,
        teacherName = string("teacherName") ?: "",
        studentName = string("studentName"),
        channelName = string("channelName") ?: "",
        orderPrice = string("orderPrice") ?: numberString("orderPrice"),
        payAmount = string("payAmount") ?: numberString("payAmount"),
        payType = string("payType")
    )

private fun JsonObject.toReserveDetail(): ReserveDetail =
    ReserveDetail(
        reserveId = long("reserveId") ?: 0L,
        status = string("status") ?: "",
        userId = long("userId") ?: 0L,
        studentName = string("studentName"),
        reserveCreateTime = string("reserveCreateTime") ?: "",
        courseStartTime = string("courseStartTime") ?: "",
        hour = int("hour") ?: 0,
        teacherId = long("teacherId") ?: 0L,
        teacherName = string("teacherName") ?: "",
        teacherAvatar = string("teacherAvatar"),
        orderPrice = string("orderPrice") ?: numberString("orderPrice"),
        payAmount = string("payAmount") ?: numberString("payAmount"),
        payType = string("payType"),
        channelName = string("channelName"),
        cancelUserId = long("cancelUserId") ?: long("cancelByUserId"),
        cancelRole = string("cancelRole") ?: string("cancelByRole") ?: string("cancelledByRole")
            ?: string("cancelUserRole"),
        cancelOperator = string("cancelOperator")
    )

private fun JsonObject.toReserveClassroomDetail(): ReserveClassroomDetail =
    ReserveClassroomDetail(
        status = string("status") ?: "",
        counterpartUserId = long("counterpartUserId") ?: 0L,
        counterpartUserName = string("counterpartUserName") ?: string("counterpartName")
            ?: string("userName") ?: string("nickName"),
        counterpartRole = string("counterpartRole") ?: "",
        reserveCreateTime = string("reserveCreateTime") ?: "",
        courseStartTime = string("courseStartTime") ?: "",
        hour = int("hour") ?: 0,
        cancelUserId = long("cancelUserId") ?: long("cancelByUserId"),
        cancelRole = string("cancelRole") ?: string("cancelByRole") ?: string("cancelledByRole")
            ?: string("cancelUserRole"),
        cancelOperator = string("cancelOperator")
    )

private fun JsonObject.obj(key: String): JsonObject =
    this[key] as? JsonObject ?: JsonObject(emptyMap())

private fun JsonObject.array(key: String): List<JsonElement> =
    (this[key] as? JsonArray)?.toList().orEmpty()

private fun JsonObject.string(key: String): String? =
    (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content

private fun JsonObject.numberString(key: String): String? =
    (this[key] as? JsonPrimitive)?.content

private fun JsonObject.long(key: String): Long? =
    (this[key] as? JsonPrimitive)?.longOrNull

private fun JsonObject.int(key: String): Int? =
    (this[key] as? JsonPrimitive)?.intOrNull

private fun JsonObject.float(key: String): Float? =
    (this[key] as? JsonPrimitive)?.content?.toFloatOrNull()

private fun JsonObject.boolean(key: String): Boolean? =
    (this[key] as? JsonPrimitive)?.booleanOrNull
