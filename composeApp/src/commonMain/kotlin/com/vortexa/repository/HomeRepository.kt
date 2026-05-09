package com.vortexa.repository

import com.vortexa.model.AuthorInfo
import com.vortexa.model.CommentReplyItem
import com.vortexa.model.CreatePostResponse
import com.vortexa.model.PostCommentItem
import com.vortexa.model.PostDetailResponse
import com.vortexa.model.PostImageUploadData
import com.vortexa.model.PostInfo
import com.vortexa.model.RecommendCourseItem
import com.vortexa.model.RecommendCourseResponse
import com.vortexa.model.RecommendPostResponse
import com.vortexa.model.RecommendTeacherResponse

class HomeRepository {
    suspend fun getRecommendPosts(pageNum: Int = 1, pageSize: Int = 3): Result<RecommendPostResponse> =
        Result.success(RecommendPostResponse(total = 0, pageNum = pageNum, pageSize = pageSize, list = emptyList()))

    suspend fun getRecommendCourse(pageNum: Int = 1, pageSize: Int = 4, userId: Long? = null): Result<RecommendCourseResponse> =
        Result.success(RecommendCourseResponse(pageNum = pageNum, pageSize = pageSize, total = 0, list = emptyList()))

    suspend fun getRecommendTeachers(pageNum: Int = 1, pageSize: Int = 4, userId: Long? = null): Result<RecommendTeacherResponse> =
        Result.success(RecommendTeacherResponse(total = 0, pageNum = pageNum, pageSize = pageSize, list = emptyList()))

    suspend fun getDiscussionPosts(pageNum: Int = 1, pageSize: Int = 4, postType: Int): Result<RecommendPostResponse> =
        Result.success(RecommendPostResponse(total = 0, pageNum = pageNum, pageSize = pageSize, list = emptyList()))

    suspend fun getSearchSuggest(): Result<List<String>> = Result.success(emptyList())

    suspend fun getPostDetail(postId: Long): Result<PostDetailResponse> = Result.success(
        PostDetailResponse(
            authorInfo = AuthorInfo(postId, null, "Vortexa", false),
            postInfo = PostInfo(
                postId = postId,
                title = "",
                content = "",
                module = null,
                board = null,
                likeCount = 0,
                collectCount = 0,
                replyCount = 0,
                isCollect = false,
                publishTime = "",
                mediaList = emptyList(),
                totalMediaCount = 0,
            ),
        ),
    )

    suspend fun getPostComments(postId: Long, pageNum: Int = 1, pageSize: Int = 5, userId: Long? = null): Result<List<PostCommentItem>> =
        Result.success(emptyList())

    suspend fun getCommentReplies(commentId: Long, pageNum: Int = 1, pageSize: Int = 10): Result<List<CommentReplyItem>> =
        Result.success(emptyList())

    suspend fun postComment(postId: Long, parentCommentId: Long? = null, content: String, mediaList: List<String>? = null): Result<Unit> =
        Result.success(Unit)

    suspend fun createPost(title: String, content: String, module: String, mediaList: List<String>? = null): Result<CreatePostResponse> =
        Result.success(CreatePostResponse(postId = 0L))

    suspend fun updatePost(postId: Long, title: String, content: String, module: String, mediaList: List<String>? = null): Result<Unit> =
        Result.success(Unit)

    suspend fun uploadPostImage(uri: Any): Result<PostImageUploadData> = Result.success(PostImageUploadData(""))
}

