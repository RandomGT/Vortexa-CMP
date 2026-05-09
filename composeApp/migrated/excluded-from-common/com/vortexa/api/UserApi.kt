package com.vortexa.api

import com.vortexa.lib_net.model.ApiResponse
import com.vortexa.model.UserCenterInfo
import com.vortexa.model.AvatarUploadData
import com.vortexa.model.FollowResult
import com.vortexa.model.UserCenterUpdateData
import com.vortexa.model.UserCenterUpdateRequest
import com.vortexa.model.UserProfileResponse
import com.vortexa.model.WalletPointData
import com.vortexa.model.UserPostsRequest
import com.vortexa.model.UserPostsResponse
import com.vortexa.model.CollectionRequest
import com.vortexa.model.CollectionResponse
import com.vortexa.model.FollowingListResponse
import com.vortexa.model.DynamicPostsResponse
import com.vortexa.model.ViewHistoryResponse
import com.vortexa.model.InteractionRequest
import com.vortexa.model.InteractionResponse
import com.vortexa.model.LikePostData
import com.vortexa.model.LikeCommentData
import com.vortexa.model.DeletePostData
import com.vortexa.model.UserCenterCommentsResponse
import com.vortexa.model.UpdatePostRequest
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 用户相关接口
 */
interface UserApi {

    @GET("/v/api/user/profile/{userId}")
    suspend fun getUserProfile(
        @Path("userId") userId: Long
    ): ApiResponse<UserProfileResponse>

    /**
     * 获取关注列表（按关注顺序排序）。
     *
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @param type 过滤类型：0 默认，1 按最近访问排序
     */
    @GET("/v/api/dynamic/followingList")
    suspend fun getFollowingList(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("Type") type: Int = 0
    ): ApiResponse<FollowingListResponse>

    /**
     * 获取动态（关注流帖子）。不传 followingId 时返回所有关注者动态按时间排序。
     *
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 20
     */
    @GET("/v/api/dynamic/posts")
    suspend fun getDynamicPosts(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<DynamicPostsResponse>

    /**
     * 关注用户。
     *
     * @param userId 目标用户 ID
     * @return ApiResponse<FollowResult>
     */
    @POST("/v/api/user/follow/{userId}")
    suspend fun follow(@Path("userId") userId: Long): ApiResponse<FollowResult>

    /**
     * 取消关注用户。
     *
     * @param userId 目标用户 ID
     * @return ApiResponse<FollowResult>
     */
    @DELETE("/v/api/user/follow/{userId}")
    suspend fun unfollow(@Path("userId") userId: Long): ApiResponse<FollowResult>

    /**
     * 点赞帖子。
     *
     * @param postId 帖子 ID
     * @return ApiResponse<LikePostData>
     */
    @POST("/v/api/user/like/post/{postId}")
    suspend fun likePost(@Path("postId") postId: Long): ApiResponse<LikePostData>

    /**
     * 取消点赞帖子。
     *
     * @param postId 帖子 ID
     * @return ApiResponse<LikePostData>
     */
    @DELETE("/v/api/user/like/post/{postId}")
    suspend fun unlikePost(@Path("postId") postId: Long): ApiResponse<LikePostData>

    /**
     * 收藏帖子。
     *
     * @param postId 帖子 ID
     * @return ApiResponse<LikePostData> data.postId 为帖子 ID
     */
    @POST("/v/api/user/collect/post/{postId}")
    suspend fun collectPost(@Path("postId") postId: Long): ApiResponse<LikePostData>

    /**
     * 取消收藏帖子。
     *
     * @param postId 帖子 ID
     * @return ApiResponse<LikePostData> data.postId 为帖子 ID
     */
    @DELETE("/v/api/user/collect/post/{postId}")
    suspend fun uncollectPost(@Path("postId") postId: Long): ApiResponse<LikePostData>

    /**
     * 点赞评论。
     *
     * @param commentId 评论 ID
     * @return ApiResponse<LikeCommentData>
     */
    @POST("/v/api/user/like/comment/{commentId}")
    suspend fun likeComment(@Path("commentId") commentId: Long): ApiResponse<LikeCommentData>

    /**
     * 取消点赞评论。
     *
     * @param commentId 评论 ID
     * @return ApiResponse<LikeCommentData>
     */
    @DELETE("/v/api/user/like/comment/{commentId}")
    suspend fun unlikeComment(@Path("commentId") commentId: Long): ApiResponse<LikeCommentData>

    /**
     * 获取个人中心信息（头像、昵称、发帖/获赞/关注/粉丝等统计数据）
     *
     * @return ApiResponse<UserCenterInfo>
     */
    @GET("/v/api/user/center/info")
    suspend fun getUserCenterInfo(): ApiResponse<UserCenterInfo>

    /**
     * 编辑头像/昵称。avatar、userName 为可选，不修改可不传。
     *
     * @param request userId 必填，avatar/userName 可选
     * @return ApiResponse<UserCenterUpdateData>
     */
    @POST("/v/api/user/center/update")
    suspend fun updateUserCenter(@Body request: UserCenterUpdateRequest): ApiResponse<UserCenterUpdateData>

    /**
     * 上传头像，返回 CDN URL。用于编辑资料时新头像需先上传获 URL 再调用 update。
     *
     * @param file 图片文件 Part，字段名 avatar
     * @return ApiResponse<AvatarUploadData>
     */
    @Multipart
    @POST("/v/api/user/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): ApiResponse<AvatarUploadData>

    /**
     * 查看互动管理（actorType、actionType、direction、分页）。
     *
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @return ApiResponse<InteractionResponse>
     */
    @POST("/v/api/user/interactions")
    suspend fun getInteractions(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Body request: InteractionRequest
    ): ApiResponse<InteractionResponse>

    /**
     * 查看我的收藏。
     *
     * @param request body，含 module：板块中文名（如杂谈、交易经验、玩法），null/不传表示全部
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 20
     */
    @POST("/v/api/user/collections")
    suspend fun getCollections(
        @Body request: CollectionRequest,
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<CollectionResponse>

    /**
     * 查看浏览记录。
     *
     * @param module 板块名：null 表示全部，否则传与筛选 chip 一致的文案，如「杂谈」「交易经验」「玩法」
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 20
     */
    @GET("/v/api/user/viewHistory")
    suspend fun getViewHistory(
        @Query("module") module: String? = null,
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<ViewHistoryResponse>

    /**
     * 获取积分余额。
     *
     * @return ApiResponse<WalletPointData> data.availablePoints 为可用积分
     */
    @GET("/v/api/user/wallet/point")
    suspend fun getWalletPoint(): ApiResponse<WalletPointData>

    /**
     * 获取稿件管理页面列表。
     *
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @param request Body：status、searchKeyword、sortBy
     * @return ApiResponse<UserPostsResponse>
     */
    @POST("/v/api/user/posts")
    suspend fun getPosts(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Body request: UserPostsRequest
    ): ApiResponse<UserPostsResponse>

    /**
     * 编辑贴文（仅作者）。
     *
     * @param postId 帖子 ID（路径）
     * @param request module、title、content、mediaList（可选 JSON 字符串）
     * @return data 多为帖子 id 等业务数字
     */
    @PUT("/v/api/user/posts/update/{postId}")
    suspend fun updatePost(
        @Path("postId") postId: Long,
        @Body request: UpdatePostRequest
    ): ApiResponse<Long?>

    /**
     * 删除单个贴文（逻辑删除，仅作者）。
     *
     * @param postId 帖子 ID（路径参数）
     */
    @DELETE("/v/api/user/posts/{postId}")
    suspend fun deletePost(@Path("postId") postId: Long): ApiResponse<DeletePostData?>

    /**
     * 获取指定用户发帖列表（个人中心/他人主页）。
     *
     * @param userId 用户 ID
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 5
     */
    @GET("/v/api/user/center/info/posts")
    suspend fun getUserCenterPosts(
        @Query("userId") userId: Long,
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 5
    ): ApiResponse<DynamicPostsResponse>

    /**
     * 获取指定用户回复/评论列表（个人中心/他人主页）。
     *
     * @param userId 用户 ID
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 10
     */
    @GET("/v/api/user/center/info/comments")
    suspend fun getUserCenterComments(
        @Query("userId") userId: Long,
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): ApiResponse<UserCenterCommentsResponse>
}
