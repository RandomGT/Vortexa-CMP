package com.vortexa.api

import com.vortexa.lib_net.model.ApiResponse
import com.vortexa.model.CreatorActivityListResponse
import com.vortexa.model.CreatorData
import com.vortexa.model.PostDataListResponse
import com.vortexa.model.CreatorTaskListResponse
import com.vortexa.model.CreatorProfileData
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 创作中心相关接口
 */
interface CreatorApi {

    /**
     * 获取近 x 日创作数据（发帖、浏览、点赞、评论等）
     *
     * @param days 统计天数，默认 7
     * @return ApiResponse<CreatorData>
     */
    @GET("/v/api/user/creator/data/{days}")
    suspend fun getCreatorData(
        @Path("days") days: Int
    ): ApiResponse<CreatorData>

    /**
     * 获取创作中心用户信息（头像、昵称、认证标签）
     *
     * @param userId 用户 ID
     * @return ApiResponse<CreatorProfileData>
     */
    @GET("/v/api/user/profile/{userId}")
    suspend fun getCreatorUserInfo(
        @Path("userId") userId: Long
    ): ApiResponse<CreatorProfileData>

    /**
     * 获取创作中心活动列表（Banner 数据）
     *
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 4
     * @return ApiResponse<CreatorActivityListResponse>
     */
    @GET("/v/api/user/creator/activities")
    suspend fun getCreatorActivities(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 4
    ): ApiResponse<CreatorActivityListResponse>

    /**
     * 获取激励任务列表。
     *
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 3
     * @return ApiResponse<CreatorTaskListResponse>
     */
    @GET("/v/api/user/creator/tasks")
    suspend fun getCreatorTasks(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 3
    ): ApiResponse<CreatorTaskListResponse>

    /**
     * 获取贴文近 x 日数据一览列表（数据中心底部列表）。
     * GET /v/api/user/posts/data/{days}
     *
     * @param days 统计天数，必填，默认 7
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @param sortBy 排序：0 默认(发帖时间)，1 最多点击，2 最多回复，3 最多点赞，4 最多转发，5 最高收益
     * @return ApiResponse<PostDataListResponse>
     */
    @GET("/v/api/user/posts/data/{days}")
    suspend fun getPostDataList(
        @Path("days") days: Int,
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("sortBy") sortBy: Int = 0
    ): ApiResponse<PostDataListResponse>
}
