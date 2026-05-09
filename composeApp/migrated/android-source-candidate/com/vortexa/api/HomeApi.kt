package com.vortexa.api

import com.vortexa.lib_net.model.ApiResponse
import com.vortexa.model.CommentReplyItem
import com.vortexa.model.PostCommentItem
import com.vortexa.model.PostDetailResponse
import com.vortexa.model.RecommendPostResponse
import com.vortexa.model.RecommendCourseResponse
import com.vortexa.model.RecommendTeacherResponse
import com.vortexa.model.SearchSuggestResponse
import com.vortexa.model.CreatePostRequest
import com.vortexa.model.CreatePostResponse
import com.vortexa.model.PostCommentRequest
import com.vortexa.model.PostImageUploadData
import com.vortexa.ui.base.BaseListResult
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Part
import retrofit2.http.Query

interface HomeApi {

    @GET("/v/api/home/recommend/post")
    suspend fun getRecommendPosts(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 3
    ): ApiResponse<RecommendPostResponse>

    /**
     * 获取推荐课程（涡联学院），支持分页与可选个性化推荐。
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 4
     * @param userId 当前用户 ID，可选，用于个性化推荐
     */
    @GET("/v/api/home/course")
    suspend fun getRecommendCourse(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 4,
        @Query("userId") userId: Long? = null
    ): ApiResponse<RecommendCourseResponse>

    @GET("/v/api/home/teacher")
    suspend fun getRecommendTeachers(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 4,
        @Query("userId") userId: Long? = null
    ): ApiResponse<RecommendTeacherResponse>

    /**
     * 获取交流页推荐帖子，按帖子分区筛选。
     * @param postType 帖子分区：1 综合，2 杂谈，3 交易经验，4 玩法
     */
    @GET("/v/api/home/discussion/post")
    suspend fun getDiscussionPosts(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 4,
        @Query("postType") postType: Int
    ): ApiResponse<RecommendPostResponse>

    /**
     * 获取搜索提示（热搜话题），不传 keyword。
     */
    @GET("/v/api/home/search/suggest")
    suspend fun getSearchSuggest(): ApiResponse<SearchSuggestResponse>

    /**
     * 查看贴文详情。
     * @param postId 贴文 ID，必填
     */
    @GET("/v/api/home/posts/{postId}")
    suspend fun getPostDetail(@Path("postId") postId: Long): ApiResponse<PostDetailResponse>

    /**
     * 获取帖子一级评论（parentCommentId 为 null）。
     * @param postId 贴文 ID，必填
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 5
     * @param userId 可选，「只看TA」时传入指定用户 ID
     */
    @GET("/v/api/home/posts/{postId}/comment")
    suspend fun getPostComments(
        @Path("postId") postId: Long,
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 5,
        @Query("userId") userId: Long? = null
    ): ApiResponse<BaseListResult<PostCommentItem>>

    /**
     * 获取评论回复（parentCommentId 为 commentId 的项）。
     * @param commentId 评论 ID，必填
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 5
     */
    @GET("/v/api/home/comments/{commentId}/replies")
    suspend fun getCommentReplies(
        @Path("commentId") commentId: Long,
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 5
    ): ApiResponse<BaseListResult<CommentReplyItem>>

    /**
     * 发布贴文。
     * @param request title、content、module、mediaList
     * @return 成功时 message 如 "发帖成功，请等待审核"
     */
    @POST("/v/api/home/post/insert")
    suspend fun createDiscussionPost(@Body request: CreatePostRequest): ApiResponse<CreatePostResponse?>

    /**
     * 上传贴文图片。
     * @param file 图片文件，multipart 字段名固定为 file
     * @return 上传成功后返回图片 URL
     */
    @Multipart
    @POST("/v/api/home/post/image")
    suspend fun uploadPostImage(@Part file: MultipartBody.Part): ApiResponse<PostImageUploadData>

    /**
     * 发布评论/回复。
     * @param request postId、parentCommentId（空=对贴文评论，有值=对该评论回复）、content
     * @return 成功时 message 如 "评论成功"
     */
    @POST("/v/api/home/discussion/comments")
    suspend fun postComment(@Body request: PostCommentRequest): ApiResponse<Unit?>
}
