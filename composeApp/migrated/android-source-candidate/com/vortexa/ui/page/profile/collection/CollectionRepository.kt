package com.vortexa.ui.page.profile.collection

import android.util.Log
import com.vortexa.api.UserApi
import com.vortexa.lib_net.client.RetrofitClient
import com.vortexa.lib_net.extension.getDataOrThrow
import com.vortexa.model.CollectionItem
import com.vortexa.model.CollectionRequest
import com.vortexa.model.CollectionResponse
import com.vortexa.model.Post

/**
 * 收藏列表 Repository。
 * 负责 POST /v/api/user/collections 接口调用与数据转换（module 在 body）。
 *
 * @author LuXin
 */
class CollectionRepository {

    private val api: UserApi by lazy {
        RetrofitClient.createService()
    }

    /**
     * 获取收藏列表。
     *
     * @param module 板块中文名（如「杂谈」）；传 null 表示全部
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @return Result<CollectionResponse> 成功返回分页结果
     */
    suspend fun getCollections(
        module: String? = null,
        pageNum: Int = 1,
        pageSize: Int = 20
    ): Result<CollectionResponse> = runCatching {
        Log.d(TAG, "getCollections: module=$module, pageNum=$pageNum, pageSize=$pageSize")
        val request = CollectionRequest(module = module)
        val response = api.getCollections(
            request = request,
            pageNum = pageNum,
            pageSize = pageSize
        )
        response.getDataOrThrow()
    }

    /**
     * 将 CollectionItem 映射为 Post，供 PostItem 展示。
     */
    fun mapToPost(item: CollectionItem): Post {
        val published = item.publishTime?.trim().orEmpty()
        return Post(
            id = item.postId.toString(),
            username = item.nickname,
            avatar = item.authorAvatar,
            time = published,
            content = item.summary,
            title = item.title,
            publishTime = item.publishTime,
            likeCount = item.likeCount,
            commentCount = item.replyCount,
            isLiked = item.isLiked,
            isCollect = true,
            userId = item.authorId,
            module = item.module,
            collectCount = item.collectCount,
            images = item.mediaList?:emptyList()
        )
    }

    companion object {
        private const val TAG = "CollectionRepository"

        /**
         * 收藏页筛选索引与接口 [CollectionRequest.module] 的对应关系（与 [CollectionFilter] Chip 顺序一致）。
         */
        fun moduleParamForFilterIndex(filterIndex: Int): String? = when (filterIndex) {
            1 -> "杂谈"
            2 -> "交易经验"
            3 -> "玩法"
            else -> null
        }
    }
}
