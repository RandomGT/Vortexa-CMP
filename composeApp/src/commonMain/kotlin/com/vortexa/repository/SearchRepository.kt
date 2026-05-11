package com.vortexa.repository

import com.vortexa.model.Post
import com.vortexa.model.SearchResultListItem
import com.vortexa.model.SearchResultRequest
import com.vortexa.net.SearchApi

class SearchRepository(
    private val api: SearchApi = SearchApi()
) {
    suspend fun getSearchResult(keyword: String, type: String = "general", pageNum: Int = 1, pageSize: Int = 4): Result<List<Post>> =
        runCatching {
            api.getSearchResult(
                pageNum = pageNum,
                pageSize = pageSize,
                request = SearchResultRequest(keyword = keyword, type = type)
            ).list
                .filter { it.type.equals("post", ignoreCase = true) }
                .map { it.toPost() }
        }
}

private fun SearchResultListItem.toPost(): Post = Post(
    id = (postId ?: 0L).toString(),
    username = nickname ?: "",
    avatar = avatar,
    time = publishTime ?: "",
    content = summary ?: "",
    images = mediaList ?: emptyList(),
    tagName = module,
    likeCount = likeCount ?: 0,
    commentCount = replyCount ?: 0,
    isLiked = isLiked,
    isCollect = isCollect,
    userId = userId ?: 0L,
    title = title,
    summary = summary,
    totalMediaCount = totalMediaCount ?: mediaList?.size ?: 0,
    module = module,
    isInteractionHot = isInteractionHot,
    isViewHot = isViewHot,
    collectCount = collectCount ?: 0,
    publishTime = publishTime
)
