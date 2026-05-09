package com.vortexa.repository

import android.util.Log
import com.vortexa.api.C2cApi
import com.vortexa.lib_net.client.RetrofitClient
import com.vortexa.lib_net.exception.ApiException
import com.vortexa.model.ReserveClassroomDetail
import com.vortexa.model.ReserveDetail
import com.vortexa.model.ReserveListItem
import com.vortexa.model.RtcChannelUserProfile
import com.vortexa.model.TeacherDetailResponse
import com.vortexa.model.TeacherListResponse
import com.vortexa.model.ReserveAcceptRequest
import com.vortexa.model.ReserveCancelRequest
import com.vortexa.model.ReserveRejectRequest
import com.vortexa.model.TeacherReserveReceipt
import com.vortexa.model.TeacherReserveRequest
import com.vortexa.model.TeacherReserveTimeItem

/**
 * C2C 业务数据仓库，负责导师列表等接口调用与数据解包。
 */
class C2cRepository {

    private companion object {
        const val TAG = "C2cRepository"
    }

    private val api: C2cApi by lazy {
        RetrofitClient.createService()
    }

    private val userRepository by lazy { UserRepository() }

    /**
     * 获取导师个人主页详情。
     * @param teacherId 导师 ID
     * @return Result 成功时为 TeacherDetailResponse，失败时包含 ApiException
     */
    suspend fun getTeacherDetail(teacherId: Long): Result<TeacherDetailResponse> = runCatching {
        val response = api.getTeacherDetail(teacherId)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    /**
     * 获取导师列表，支持标签与报价筛选。
     * @param tags 多个标签英文逗号分隔，null 表示不按标签筛
     * @param minPrice 最低报价，null 表示不限制
     * @param maxPrice 最高报价，null 表示不限制
     * @return Result 成功时为 TeacherListResponse，失败时包含 ApiException
     */
    suspend fun getTeacherList(
        tags: String? = null,
        minPrice: String? = null,
        maxPrice: String? = null,
        pageNum: Int = 1,
        pageSize: Int = 5
    ): Result<TeacherListResponse> = runCatching {
        val response = api.getTeacherList(tags, minPrice, maxPrice, pageNum, pageSize)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    /**
     * 查询某导师某天的预约时间。
     * @param teacherId 导师 ID
     * @param reserveDate 预约日期，格式 yyyy/MM/dd
     * @return Result 成功时为时段列表，失败时包含 ApiException
     */
    suspend fun getTeacherReserveTime(teacherId: Long, reserveDate: String): Result<List<TeacherReserveTimeItem>> =
        runCatching {
            val response = api.getTeacherReserveTime(teacherId, reserveDate)
            if (!response.isSuccess) {
                throw ApiException(response.code, response.message)
            }
            response.data ?: emptyList()
        }

    /**
     * 预约一对一指导。
     * @param teacherId 导师 ID
     * @param reserveDate 预约日期，格式 yyyy/MM/dd
     * @param reserveHour 时段，如 18:00-19:00
     * @param userId 预约用户 ID
     * @return Result 成功时为预约回执，失败时包含 ApiException
     */
    suspend fun reserve(
        teacherId: Long,
        reserveDate: String,
        reserveHour: String,
        userId: Long
    ): Result<TeacherReserveReceipt> = runCatching {
        val request = TeacherReserveRequest(
            teacherId = teacherId.toInt(),
            reserveDate = reserveDate,
            reserveHour = reserveHour,
            userId = userId
        )
        val response = api.reserve(request)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    /**
     * 预约列表，学员端/导师端共用。
     * @param type 1-学生端 2-导师端
     * @param status 可选筛选项，与接口约定一致；null 或空白表示全部
     * @return Result 成功时为列表，失败时包含 ApiException
     */
    suspend fun getReserveList(type: Int, status: String? = null): Result<List<ReserveListItem>> =
        runCatching {
            val statusQuery = status?.trim()?.takeIf { it.isNotEmpty() }
            val response = api.getReserveList(type, statusQuery)
            if (!response.isSuccess) {
                throw ApiException(response.code, response.message)
            }
            response.data ?: emptyList()
        }

    /**
     * 获取预约详情。
     * @param reserveId 预约 ID
     * @return Result 成功时为 ReserveDetail，失败时包含 ApiException
     */
    suspend fun getReserveDetail(reserveId: Int): Result<ReserveDetail> = runCatching {
        val response = api.getReserveDetail(reserveId)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    /**
     * 课堂小助手详情（GET /v/api/c2c/teacher/reserve/classroom）。
     */
    suspend fun getReserveClassroom(reserveId: Int): Result<ReserveClassroomDetail> = runCatching {
        val response = api.getReserveClassroom(reserveId)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    /**
     * 取消预约。
     * @param reserveId 预约 ID
     * @param reason 取消原因，必填
     * @return Result 成功为 Unit，失败包含 ApiException
     */
    suspend fun cancelReserve(reserveId: Int, reason: String): Result<Unit> = runCatching {
        val response = api.cancelReserve(ReserveCancelRequest(reserveId = reserveId, reason = reason))
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
    }

    /**
     * 导师接受预约（仅导师）。
     */
    suspend fun acceptReserve(reserveId: Int): Result<Unit> = runCatching {
        val response = api.acceptReserve(ReserveAcceptRequest(reserveId = reserveId))
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
    }

    /**
     * 导师拒绝预约（仅导师）。
     * @param reason 拒绝原因，可选
     */
    suspend fun rejectReserve(reserveId: Int, reason: String? = null): Result<Unit> = runCatching {
        val response = api.rejectReserve(ReserveRejectRequest(reserveId = reserveId, reason = reason))
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
    }

    /**
     * 获取声网 Token。
     * @param channelName 频道名
     * @return Result 成功时为 Token 字符串，失败时包含 ApiException
     */
    suspend fun getC2cToken(channelName: String): Result<String> = runCatching {
        val response = api.getC2cToken(channelName)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Token 为空")
    }

    /**
     * 根据声网 uid 查询昵称、头像；业务 UID 与声网 uid 一致，走 [UserRepository.getUserProfile]。
     *
     * @param agoraUid 声网 uid
     * @return Result 成功时返回昵称、头像等业务信息
     */
    suspend fun getRtcChannelUserProfile(agoraUid: Int): Result<RtcChannelUserProfile> {
        Log.i(TAG, "getRtcChannelUserProfile: agoraUid=$agoraUid")
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
