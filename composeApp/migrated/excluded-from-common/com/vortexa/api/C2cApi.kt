package com.vortexa.api

import com.vortexa.lib_net.model.ApiResponse
import com.vortexa.model.TeacherDetailResponse
import com.vortexa.model.TeacherListResponse
import com.vortexa.model.ReserveClassroomDetail
import com.vortexa.model.ReserveDetail
import com.vortexa.model.ReserveListApiStatus
import com.vortexa.model.ReserveListItem
import com.vortexa.model.ReserveAcceptRequest
import com.vortexa.model.ReserveCancelRequest
import com.vortexa.model.ReserveRejectRequest
import com.vortexa.model.TeacherReserveReceipt
import com.vortexa.model.TeacherReserveRequest
import com.vortexa.model.TeacherReserveTimeItem
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * C2C 相关接口（导师列表、导师个人主页等）。
 */
interface C2cApi {

    /**
     * 获取导师个人主页详情。
     * @param teacherId 导师 ID，必填
     */
    @GET("/v/api/c2c/teacher/detail")
    suspend fun getTeacherDetail(
        @Query("teacherId") teacherId: Long
    ): ApiResponse<TeacherDetailResponse>

    /**
     * 获取导师列表，支持按标签、报价区间筛选。
     * @param tags 筛选标签，多个用英文逗号分隔，可选
     * @param minPrice 最低报价，可选
     * @param maxPrice 最高报价，可选
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 5
     */
    @GET("/v/api/c2c/teacher/list")
    suspend fun getTeacherList(
        @Query("tags") tags: String? = null,
        @Query("minPrice") minPrice: String? = null,
        @Query("maxPrice") maxPrice: String? = null,
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 5
    ): ApiResponse<TeacherListResponse>

    /**
     * 查询某导师某天的预约时间。
     * @param teacherId 导师 ID，必填
     * @param reserveDate 预约日期，格式 yyyy/MM/dd，必填
     */
    @GET("/v/api/c2c/teacher/reserve/time")
    suspend fun getTeacherReserveTime(
        @Query("teacherId") teacherId: Long,
        @Query("reserveDate") reserveDate: String
    ): ApiResponse<List<TeacherReserveTimeItem>>

    /**
     * 预约一对一指导。成功后返回预约回执。
     * @param request 预约参数：teacherId、reserveDate(yyyy/MM/dd)、reserveHour(如 18:00-19:00)、userId
     */
    @POST("/v/api/c2c/teacher/reserve")
    suspend fun reserve(@Body request: TeacherReserveRequest): ApiResponse<TeacherReserveReceipt>

    /**
     * 预约列表（学员端/导师端）。
     * @param type 1-学生端 2-导师端
     * @param status 可选查询参数，取值见 [ReserveListApiStatus]；不传或空白表示全部
     */
    @GET("/v/api/c2c/teacher/reserve/list")
    suspend fun getReserveList(
        @Query("type") type: Int,
        @Query("status") status: String? = null
    ): ApiResponse<List<ReserveListItem>>

    /**
     * 预约详情（学员端/导师端）。
     * @param reserveId 预约 ID，必填
     */
    @GET("/v/api/c2c/teacher/reserve/detail")
    suspend fun getReserveDetail(@Query("reserveId") reserveId: Int): ApiResponse<ReserveDetail>

    /**
     * 课堂小助手详情（预约课堂侧）。
     * @param reserveId 预约 ID，必填
     */
    @GET("/v/api/c2c/teacher/reserve/classroom")
    suspend fun getReserveClassroom(@Query("reserveId") reserveId: Int): ApiResponse<ReserveClassroomDetail>

    /**
     * 取消预约。
     * @param request reserveId、reason 必填
     */
    @POST("/v/api/c2c/teacher/reserve/cancel")
    suspend fun cancelReserve(@Body request: ReserveCancelRequest): ApiResponse<Unit?>

    /**
     * 导师接受一对一预约。
     */
    @POST("/v/api/c2c/teacher/reserve/accept")
    suspend fun acceptReserve(@Body request: ReserveAcceptRequest): ApiResponse<Unit?>

    /**
     * 导师拒绝一对一预约。
     */
    @POST("/v/api/c2c/teacher/reserve/reject")
    suspend fun rejectReserve(@Body request: ReserveRejectRequest): ApiResponse<Unit?>

    /**
     * 获取声网 Token。
     * @param channelName 频道名，必填
     */
    @GET("/v/api/c2c/token")
    suspend fun getC2cToken(@Query("channelName") channelName: String): ApiResponse<String>
}
