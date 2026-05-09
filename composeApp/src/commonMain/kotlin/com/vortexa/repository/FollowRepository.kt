package com.vortexa.repository

import com.vortexa.model.DynamicPostListItem
import com.vortexa.model.DynamicPostsResponse
import com.vortexa.model.FollowedUser
import com.vortexa.model.FollowingListItem
import com.vortexa.model.FollowingListResponse
import com.vortexa.model.Post
import com.vortexa.model.toPost

class FollowRepository {
    suspend fun getFollowingList(pageNum: Int = 1, pageSize: Int = 20, type: Int = 0): Result<FollowingListResponse> =
        Result.success(FollowingListResponse(pageNum, pageSize, 0, emptyList()))

    suspend fun getDynamicPosts(pageNum: Int = 1, pageSize: Int = 20): Result<DynamicPostsResponse> =
        Result.success(DynamicPostsResponse(pageNum, pageSize, 0, emptyList()))

    fun mapDynamicItemToPost(item: DynamicPostListItem): Post = item.toPost()

    fun mapToFollowedUser(item: FollowingListItem): FollowedUser = FollowedUser(
        userId = item.userId,
        nickname = item.userName.ifEmpty { "用户" },
        avatar = item.userAvatar?.takeIf { it.isNotBlank() },
        hasNewPost = item.recentInteraction > 0,
    )
}

