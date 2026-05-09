package com.vortexa.repository

import android.net.Uri
import com.vortexa.api.HomeApi
import com.vortexa.lib_net.client.RetrofitClient
import com.vortexa.lib_net.exception.ApiException
import com.vortexa.model.CreatePostRequest
import com.vortexa.model.PostCommentRequest
import com.vortexa.model.CommentReplyItem
import com.vortexa.model.PostCommentItem
import com.vortexa.model.PostDetailResponse
import com.vortexa.model.RecommendCourseResponse
import com.vortexa.model.RecommendPostResponse
import com.vortexa.model.RecommendTeacherResponse
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class HomeRepository {

    private companion object {
        const val TAG = "HomeRepository"
    }

    private val api: HomeApi by lazy {
        RetrofitClient.createService()
    }

    suspend fun getRecommendPosts(pageNum: Int = 1, pageSize: Int = 3): Result<RecommendPostResponse> = runCatching {
        val response = api.getRecommendPosts(pageNum, pageSize)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    /**
     * 获取推荐课程（涡联学院），支持分页与可选 userId 个性化推荐。
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 4
     * @param userId 当前用户 ID，可选
     */
    suspend fun getRecommendCourse(
        pageNum: Int = 1,
        pageSize: Int = 4,
        userId: Long? = null
    ): Result<RecommendCourseResponse> = runCatching {
        val response = api.getRecommendCourse(pageNum, pageSize, userId)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    suspend fun getRecommendTeachers(pageNum: Int = 1, pageSize: Int = 4, userId: Long? = null): Result<RecommendTeacherResponse> = runCatching {
        val response = api.getRecommendTeachers(pageNum, pageSize, userId)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    /**
     * 获取交流页推荐帖子列表，最多返回 pageSize 条。
     * @param postType 帖子分区：1 综合，2 杂谈，3 交易经验，4 玩法
     */
    suspend fun getDiscussionPosts(pageNum: Int = 1, pageSize: Int = 4, postType: Int): Result<RecommendPostResponse> = runCatching {
        val response = api.getDiscussionPosts(pageNum, pageSize, postType)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    /**
     * 获取搜索提示（热搜话题），不传 keyword。
     */
    suspend fun getSearchSuggest(): Result<List<String>> = runCatching {
        val response = api.getSearchSuggest()
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data?.suggestions ?: emptyList()
    }

    /**
     * 获取贴文详情。
     * @param postId 贴文 ID
     * @return Result<PostDetailResponse> 成功返回详情，失败抛出 ApiException
     */
    suspend fun getPostDetail(postId: Long): Result<PostDetailResponse> = runCatching {
        val response = api.getPostDetail(postId)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    /**
     * 获取帖子一级评论。
     * @param postId 贴文 ID
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 5
     * @param userId 可选，「只看TA」时传入
     * @return Result<List<PostCommentItem>>
     */
    suspend fun getPostComments(
        postId: Long,
        pageNum: Int = 1,
        pageSize: Int = 5,
        userId: Long? = null
    ): Result<List<PostCommentItem>> = runCatching {
        val response = api.getPostComments(postId, pageNum, pageSize, userId)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data?.list ?: emptyList()
    }

    /**
     * 获取评论回复。
     * @param commentId 评论 ID
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 5
     */
    suspend fun getCommentReplies(
        commentId: Long,
        pageNum: Int = 1,
        pageSize: Int = 5
    ): Result<List<CommentReplyItem>> = runCatching {
        val response = api.getCommentReplies(commentId, pageNum, pageSize)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data?.list ?: emptyList()
    }

    /**
     * 发布贴文。
     * @param title 标题
     * @param content 正文
     * @param module 板块（综合/杂谈/交易经验/玩法）
     * @param mediaList 可选媒体列表
     */
    suspend fun createPost(
        title: String,
        content: String,
        module: String,
        mediaList: List<String>? = null
    ): Result<Unit> = runCatching {
        val response = api.createDiscussionPost(
            CreatePostRequest(title = title, content = content, module = module, mediaList = mediaList)
        )
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        Log.i(
            TAG,
            "createPost: success, postId=${response.data?.postId ?: response.data?.id}, status=${response.data?.status}"
        )
    }

    /**
     * 上传贴文图片并返回可写入媒体列表的 URL。
     * @param uri 本地图片 Uri
     * @return Result<String> 上传成功后的图片地址
     */
    suspend fun uploadPostImage(uri: Uri): Result<String> = runCatching {
        val context = com.vortexa.VortexaApplication.instance.applicationContext
        val file = uriToFile(context, uri) ?: throw IllegalArgumentException("无法读取图片文件")
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestFile)
        Log.d(TAG, "uploadPostImage: file=${file.name}")
        val response = api.uploadPostImage(part)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data?.url ?: response.url ?: throw ApiException(-1, "Upload response url is null")
    }

    /**
     * 发布评论/回复。
     * @param postId 贴文 ID，必填
     * @param parentCommentId 父评论 ID，null 表示对贴文发表评论，有值表示对该评论进行回复
     * @param content 正文，必填
     * @param mediaList 媒体列表（图片/视频上传后的 URL），可选
     */
    suspend fun postComment(
        postId: Long,
        parentCommentId: Long?,
        content: String,
        mediaList: List<String>? = null
    ): Result<Unit> = runCatching {
        val response = api.postComment(
            PostCommentRequest(
                postId = postId,
                parentCommentId = parentCommentId,
                content = content,
                mediaList = mediaList
            )
        )
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        Log.d(TAG, "postComment: success, postId=$postId, parentCommentId=$parentCommentId")
    }

    /**
     * 将 Uri 拷贝为临时文件，用于 multipart 上传。
     * @param context 应用上下文
     * @param uri 资源 Uri
     * @return 可上传 File，失败返回 null
     */
    private fun uriToFile(context: android.content.Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File.createTempFile("post_upload_", ".jpg", context.cacheDir)
            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            file
        } catch (e: Exception) {
            Log.e(TAG, "uriToFile failed", e)
            null
        }
    }
}
