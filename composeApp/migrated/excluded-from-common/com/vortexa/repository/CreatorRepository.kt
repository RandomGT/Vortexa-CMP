package com.vortexa.repository

import android.util.Log
import com.vortexa.api.CreatorApi
import com.vortexa.config.UserConfig
import com.vortexa.lib_net.client.RetrofitClient
import com.vortexa.lib_net.exception.ApiException
import com.vortexa.model.CreatorActivityListResponse
import com.vortexa.model.CreatorData
import com.vortexa.model.CreatorPostDataPage
import com.vortexa.model.CreatorStatisticsPostItem
import com.vortexa.model.PostDataItem
import com.vortexa.model.CreatorTaskListResponse
import com.vortexa.model.CreatorProfileData
import com.vortexa.model.CreatorUserInfo

/**
 * 创作中心数据仓库。
 * 负责创作数据、用户信息、活动列表等接口调用。
 *
 * @author LuXin
 */
class CreatorRepository {

    private val api: CreatorApi by lazy {
        RetrofitClient.createService()
    }

    /**
     * 获取近 x 日创作数据。
     *
     * @param days 统计天数，默认 7
     * @return Result<CreatorData>
     */
    suspend fun getCreatorData(days: Int = 7): Result<CreatorData> = runCatching {
        Log.d(TAG, "getCreatorData: days=$days")
        val response = api.getCreatorData(days)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    /**
     * 获取创作中心用户信息。
     *
     * @return Result<CreatorUserInfo>
     */
    suspend fun getCreatorUserInfo(): Result<CreatorUserInfo> = runCatching {
        Log.d(TAG, "getCreatorUserInfo: request")
        val response = api.getCreatorUserInfo(UserConfig.getUserId())
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        val payload = response.data ?: throw ApiException(-1, "Response data is null")
        payload.toCreatorUserInfo()
    }

    private fun CreatorProfileData.toCreatorUserInfo(): CreatorUserInfo {
        val u = userInfo ?: throw ApiException(-1, "userInfo is null")
        return CreatorUserInfo(
            userId = u.userId,
            userAvatar = u.avatar,
            userName = u.nickname,
            certifications = certifications,
            isFollowed = isFollowed ?: false,
            followCount = u.followCount ?: 0,
            fanCount = u.fanCount ?: 0,
            intro = u.intro,
            isVerified = u.isVerified ?: false
        )
    }

    /**
     * 获取创作中心活动列表。
     *
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 4
     * @return Result<CreatorActivityListResponse>
     */
    suspend fun getCreatorActivities(
        pageNum: Int = 1,
        pageSize: Int = 4
    ): Result<CreatorActivityListResponse> = runCatching {
        Log.d(TAG, "getCreatorActivities: pageNum=$pageNum, pageSize=$pageSize")
        val response = api.getCreatorActivities(pageNum, pageSize)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    /**
     * 获取激励任务列表。
     *
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 3
     * @return Result<CreatorTaskListResponse>
     */
    suspend fun getCreatorTasks(
        pageNum: Int = 1,
        pageSize: Int = 3
    ): Result<CreatorTaskListResponse> = runCatching {
        Log.d(TAG, "getCreatorTasks: pageNum=$pageNum, pageSize=$pageSize")
        val response = api.getCreatorTasks(pageNum, pageSize)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    /**
     * 获取贴文近 x 日数据一览列表（GET /v/api/user/posts/data/{days}）。
     *
     * @param days 统计天数，默认 7
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @param sortBy 排序方式，默认 0（发帖时间）
     * @return Result<[CreatorPostDataPage]>
     */
    suspend fun getPostDataList(
        days: Int = 7,
        pageNum: Int = 1,
        pageSize: Int = 20,
        sortBy: Int = 0
    ): Result<CreatorPostDataPage> = runCatching {
        Log.d(TAG, "getPostDataList: days=$days, pageNum=$pageNum, sortBy=$sortBy")
        val response = api.getPostDataList(days, pageNum, pageSize, sortBy)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        val data = response.data ?: throw ApiException(-1, "Response data is null")
        CreatorPostDataPage(
            list = data.postList.map { it.toCreatorStatisticsPostItem() },
            total = data.total
        )
    }

    private fun PostDataItem.toCreatorStatisticsPostItem() = CreatorStatisticsPostItem(
        postId = postId.toLongOrNull() ?: 0L,
        nickname = authorName.orEmpty(),
        avatar = authorAvatar,
        publishTime = publishTime,
        title = title,
        summary = summary,
        viewCount = viewCount,
        likeCount = likeCount,
        replyCount = replyCount,
        shareCount = 0, // 接口未返回，占位
        revenue = 0   // 接口未返回，占位
    )

    companion object {
        private const val TAG = "CreatorRepository"
    }
}
