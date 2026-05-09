package com.vortexa.repository

import com.vortexa.api.SearchApi
import com.vortexa.lib_net.client.RetrofitClient
import com.vortexa.lib_net.exception.ApiException
import com.vortexa.model.Post
import com.vortexa.model.SearchResultRequest

/**
 * 搜索结果相关 Repository。
 */
class SearchRepository {

    private val api: SearchApi by lazy {
        RetrofitClient.createService()
    }

    /**
     * 获取搜索结果，仅解析 type 为 Post 的项。
     *
     * @param keyword 搜索关键词
     * @param type 筛选类型：general/post/user/teacher/toolbox/course
     * @param pageNum 页码
     * @param pageSize 每页条数
     */
    suspend fun getSearchResult(
        keyword: String,
        type: String = "general",
        pageNum: Int = 1,
        pageSize: Int = 4
    ): Result<List<Post>> = runCatching {
        val response = api.getSearchResult(
            pageNum = pageNum,
            pageSize = pageSize,
            request = SearchResultRequest(keyword = keyword, type = type)
        )
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        val list = response.data?.list ?: emptyList()
        list.filter { it.type == "post" }.map { item ->
            Post(
                id = (item.postId ?: 0L).toString(),
                username = item.nickname ?: "",
                avatar = item.avatar,
                time = item.publishTime ?: "",
                content = item.summary ?: "",
                images = item.mediaList ?: emptyList(),
                tagName = item.module,
                likeCount = item.likeCount ?: 0,
                commentCount = item.replyCount ?: 0,
                isLiked = false,
                isCollect = false,
                userId = item.userId ?: 0L,
                title = item.title,
                summary = item.summary,
                totalMediaCount = item.totalMediaCount ?: 0,
                module = item.module,
                collectCount = item.collectCount ?: 0,
                publishTime = item.publishTime
            )
        }
    }
}
