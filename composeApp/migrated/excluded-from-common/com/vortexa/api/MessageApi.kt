package com.vortexa.api

import com.vortexa.lib_net.model.ApiResponse
import com.vortexa.model.MessageBatchReadRequest
import com.vortexa.model.MessageListResponse
import com.vortexa.model.SystemMessageListResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 消息相关接口。
 *
 * @author LuXin
 */
interface MessageApi {

    /**
     * 获取对话框列表。
     *
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @param userId 当前用户 ID，必填
     * @return 分页的对话框列表
     */
    @GET("/v/api/message/list")
    suspend fun getMessageList(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("userId") userId: Long
    ): ApiResponse<MessageListResponse>

    /**
     * 获取系统通知列表。
     *
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @param userId 当前用户 ID，必填
     * @return 分页的系统通知列表
     */
    @GET("/v/api/message/system")
    suspend fun getSystemMessages(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("userId") userId: Long
    ): ApiResponse<SystemMessageListResponse>

    /**
     * 获取课堂小助手消息列表。
     *
     * @see com.vortexa.repository.MessageRepository.getClassroomMessages
     */
    @GET("/v/api/message/classroom")
    suspend fun getClassroomMessages(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("userId") userId: Long
    ): ApiResponse<SystemMessageListResponse>

    /**
     * 批量标记消息已读。
     *
     * @return data 为成功标记条数（如 5）
     */
    @POST("/v/api/message/read/batch")
    suspend fun batchMarkRead(
        @Body request: MessageBatchReadRequest
    ): ApiResponse<Int?>
}
