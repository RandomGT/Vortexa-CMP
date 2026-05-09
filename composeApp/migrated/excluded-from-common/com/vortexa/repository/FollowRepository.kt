package com.vortexa.repository

import android.util.Log
import com.vortexa.api.UserApi
import com.vortexa.lib_net.client.RetrofitClient
import com.vortexa.lib_net.extension.getDataOrThrow
import com.vortexa.model.DynamicPostListItem
import com.vortexa.model.DynamicPostsResponse
import com.vortexa.model.toPost
import com.vortexa.model.FollowedUser
import com.vortexa.model.FollowingListItem
import com.vortexa.model.FollowingListResponse
import com.vortexa.model.Post

/**
 * 关注页数据仓库。
 * 负责关注列表接口 GET /v/api/dynamic/followingList、动态列表 GET /v/api/dynamic/posts 调用与数据转换。
 */
class FollowRepository {

    private val api: UserApi by lazy { RetrofitClient.createService() }

    companion object {
        private const val TAG = "FollowRepository"
    }

    /**
     * 获取关注列表（按关注顺序排序）。
     *
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @param type 0 默认，1 按最近访问排序
     * @return Result<FollowingListResponse>
     */
    suspend fun getFollowingList(
        pageNum: Int = 1,
        pageSize: Int = 20,
        type: Int = 0
    ): Result<FollowingListResponse> = runCatching {
        Log.d(TAG, "getFollowingList: pageNum=$pageNum, pageSize=$pageSize, type=$type")
        api.getFollowingList(pageNum = pageNum, pageSize = pageSize, type = type).getDataOrThrow()
    }

    /**
     * 获取动态列表（关注流帖子）。不传 followingId 时返回所有关注者动态按时间排序。
     *
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @return Result<DynamicPostsResponse>
     */
    suspend fun getDynamicPosts(
        pageNum: Int = 1,
        pageSize: Int = 20
    ): Result<DynamicPostsResponse> = runCatching {
        Log.d(TAG, "getDynamicPosts: pageNum=$pageNum, pageSize=$pageSize")
        api.getDynamicPosts(pageNum = pageNum, pageSize = pageSize).getDataOrThrow()
    }

    /** 将动态列表项映射为 Post，供 PostItem 展示。 */
    fun mapDynamicItemToPost(item: DynamicPostListItem): Post = item.toPost()

    /** 将接口项映射为 FollowedUser，供横向列表展示；有头像时用 userAvatar，无则昵称首字。 */
    fun mapToFollowedUser(item: FollowingListItem): FollowedUser = FollowedUser(
        userId = item.userId,
        nickname = item.userName.ifEmpty { "用户" },
        avatar = item.userAvatar?.takeIf { it.isNotBlank() },
        hasNewPost = item.recentInteraction > 0
    )
}
