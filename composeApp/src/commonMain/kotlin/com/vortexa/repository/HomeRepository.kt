package com.vortexa.repository

import com.vortexa.model.CommentReplyItem
import com.vortexa.model.CreatePostResponse
import com.vortexa.model.PostCommentItem
import com.vortexa.model.PostDetailResponse
import com.vortexa.model.PostImageUploadData
import com.vortexa.model.RecommendCourseItem
import com.vortexa.model.RecommendCourseResponse
import com.vortexa.model.RecommendPostResponse
import com.vortexa.model.RecommendTeacherResponse
import com.vortexa.net.HomeApi

class HomeRepository(
    private val api: HomeApi = HomeApi()
) {
    suspend fun getRecommendPosts(pageNum: Int = 1, pageSize: Int = 3): Result<RecommendPostResponse> =
        runCatching { api.getRecommendPosts(pageNum, pageSize) }

    suspend fun getRecommendCourse(pageNum: Int = 1, pageSize: Int = 4, userId: Long? = null): Result<RecommendCourseResponse> =
        runCatching { api.getRecommendCourse(pageNum, pageSize, userId) }

    suspend fun getRecommendTeachers(pageNum: Int = 1, pageSize: Int = 4, userId: Long? = null): Result<RecommendTeacherResponse> =
        runCatching { api.getRecommendTeachers(pageNum, pageSize, userId) }

    suspend fun getDiscussionPosts(pageNum: Int = 1, pageSize: Int = 4, postType: Int): Result<RecommendPostResponse> =
        runCatching { api.getDiscussionPosts(pageNum, pageSize, postType) }

    suspend fun getSearchSuggest(): Result<List<String>> =
        runCatching { api.getSearchSuggest().suggestions }

    suspend fun getPostDetail(postId: Long): Result<PostDetailResponse> =
        runCatching { api.getPostDetail(postId) }

    suspend fun getPostComments(postId: Long, pageNum: Int = 1, pageSize: Int = 5, userId: Long? = null): Result<List<PostCommentItem>> =
        runCatching { api.getPostComments(postId, pageNum, pageSize, userId) }

    suspend fun getCommentReplies(commentId: Long, pageNum: Int = 1, pageSize: Int = 10): Result<List<CommentReplyItem>> =
        runCatching { api.getCommentReplies(commentId, pageNum, pageSize) }

    suspend fun postComment(postId: Long, parentCommentId: Long? = null, content: String, mediaList: List<String>? = null): Result<Unit> =
        runCatching { api.postComment(postId, parentCommentId, content, mediaList) }

    suspend fun createPost(title: String, content: String, module: String, mediaList: List<String>? = null): Result<CreatePostResponse> =
        runCatching { api.createDiscussionPost(title, content, module, mediaList) }

    suspend fun updatePost(postId: Long, title: String, content: String, module: String, mediaList: List<String>? = null): Result<Unit> =
        Result.success(Unit)

    suspend fun uploadPostImage(uri: Any): Result<PostImageUploadData> = Result.success(PostImageUploadData(""))
}
