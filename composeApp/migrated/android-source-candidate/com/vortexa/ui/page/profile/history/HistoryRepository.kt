package com.vortexa.ui.page.profile.history

import android.util.Log
import com.vortexa.api.UserApi
import com.vortexa.lib_net.client.RetrofitClient
import com.vortexa.lib_net.extension.getDataOrThrow
import com.vortexa.model.Post
import com.vortexa.model.ViewHistoryItem
import com.vortexa.model.ViewHistoryResponse

/**
 * 浏览记录 Repository。
 * 负责 GET /v/api/user/viewHistory 接口调用与数据转换。
 *
 * @author LuXin
 */
class HistoryRepository {

    private val api: UserApi by lazy {
        RetrofitClient.createService()
    }

    private companion object {
        const val TAG = "HistoryRepository"
    }

    /**
     * 获取浏览记录。
     *
     * @param module 板块名：null 全部，否则与筛选 chip 一致，如「杂谈」
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @return Result<ViewHistoryResponse> 成功返回分页结果
     */
    suspend fun getViewHistory(
        module: String? = null,
        pageNum: Int = 1,
        pageSize: Int = 20
    ): Result<ViewHistoryResponse> = runCatching {
        Log.d(TAG, "getViewHistory: module=$module, pageNum=$pageNum, pageSize=$pageSize")
        api.getViewHistory(module = module, pageNum = pageNum, pageSize = pageSize).getDataOrThrow()
    }

    /**
     * 将 ViewHistoryItem 映射为 Post，供 PostItem 展示。
     */
    fun mapToPost(item: ViewHistoryItem): Post {
        return Post(
            id = item.postId.toString(),
            username = item.nickname,
            avatar = item.avatar,
            time = item.publishTime ?: "",
            content = item.summary ?: "",
            images = item.mediaList ?: emptyList(),
            tagName = item.module,
            likeCount = item.likeCount,
            commentCount = item.replyCount,
            isLiked = item.isLiked,
            isCollect = item.isCollect,
            userId = item.userId,
            title = item.title,
            summary = item.summary,
            totalMediaCount = item.totalMediaCount,
            module = item.module,
            isInteractionHot = item.isInteractionHot,
            isViewHot = item.isViewHot,
            collectCount = item.collectCount,
            publishTime = item.publishTime,
            viewTime = item.viewTime
        )
    }
}
