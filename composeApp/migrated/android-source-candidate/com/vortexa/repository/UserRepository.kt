package com.vortexa.repository

import android.net.Uri
import android.util.Log
import com.vortexa.api.UserApi
import com.vortexa.lib_net.client.RetrofitClient
import com.vortexa.lib_net.exception.ApiException
import com.vortexa.model.UserCenterInfo
import com.vortexa.model.FollowResult
import com.vortexa.model.UserCenterUpdateData
import com.vortexa.model.UserCenterUpdateRequest
import com.vortexa.model.UserCenterCommentsResponse
import com.vortexa.model.UserProfileResponse
import com.vortexa.model.DynamicPostsResponse
import com.vortexa.model.WalletPointData
import com.vortexa.model.LikePostData
import com.vortexa.model.LikeCommentData
import com.vortexa.model.DeletePostData
import com.vortexa.model.UpdatePostRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * 用户相关数据仓库
 *
 * @author LuXin
 */
class UserRepository {

    private val api: UserApi by lazy {
        RetrofitClient.createService()
    }

    /**
     * 获取个人主页信息
     *
     * @param userId 目标用户 ID
     * @return Result<UserProfileResponse>
     */
    suspend fun getUserProfile(userId: Long): Result<UserProfileResponse> = runCatching {
        Log.d(TAG, "getUserProfile: userId=$userId")
        val response = api.getUserProfile(userId)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    /**
     * 关注用户。
     *
     * @param userId 目标用户 ID
     * @return Result<FollowResult>
     */
    suspend fun follow(userId: Long): Result<FollowResult> = runCatching {
        Log.d(TAG, "follow: userId=$userId")
        val response = api.follow(userId)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: FollowResult(userId = userId)
    }

    /**
     * 取消关注用户。
     *
     * @param userId 目标用户 ID
     * @return Result<FollowResult>
     */
    suspend fun unfollow(userId: Long): Result<FollowResult> = runCatching {
        Log.d(TAG, "unfollow: userId=$userId")
        val response = api.unfollow(userId)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: FollowResult(userId = userId)
    }

    /**
     * 点赞帖子。
     *
     * @param postId 帖子 ID
     * @return Result<LikePostData> 成功时返回 data，失败抛出 ApiException
     */
    suspend fun likePost(postId: Long): Result<LikePostData> = runCatching {
        Log.d(TAG, "likePost: postId=$postId")
        val response = api.likePost(postId)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: LikePostData(postId = postId)
    }

    /**
     * 取消点赞帖子。
     *
     * @param postId 帖子 ID
     * @return Result<LikePostData> 成功时返回 data，失败抛出 ApiException
     */
    suspend fun unlikePost(postId: Long): Result<LikePostData> = runCatching {
        Log.d(TAG, "unlikePost: postId=$postId")
        val response = api.unlikePost(postId)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: LikePostData(postId = postId)
    }

    /**
     * 收藏帖子。
     *
     * @param postId 帖子 ID
     * @return Result<LikePostData> 成功时返回 data，失败抛出 ApiException
     */
    suspend fun collectPost(postId: Long): Result<LikePostData> = runCatching {
        Log.d(TAG, "collectPost: postId=$postId")
        val response = api.collectPost(postId)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: LikePostData(postId = postId)
    }

    /**
     * 取消收藏帖子。
     *
     * @param postId 帖子 ID
     * @return Result<LikePostData> 成功时返回 data，失败抛出 ApiException
     */
    suspend fun uncollectPost(postId: Long): Result<LikePostData> = runCatching {
        Log.d(TAG, "uncollectPost: postId=$postId")
        val response = api.uncollectPost(postId)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: LikePostData(postId = postId)
    }

    /**
     * 点赞评论。
     *
     * @param commentId 评论 ID
     * @return Result<LikeCommentData> 成功时返回 data，失败抛出 ApiException
     */
    suspend fun likeComment(commentId: Long): Result<LikeCommentData> = runCatching {
        Log.d(TAG, "likeComment: commentId=$commentId")
        val response = api.likeComment(commentId)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: LikeCommentData(commentId = commentId)
    }

    /**
     * 取消点赞评论。
     *
     * @param commentId 评论 ID
     * @return Result<LikeCommentData> 成功时返回 data，失败抛出 ApiException
     */
    suspend fun unlikeComment(commentId: Long): Result<LikeCommentData> = runCatching {
        Log.d(TAG, "unlikeComment: commentId=$commentId")
        val response = api.unlikeComment(commentId)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: LikeCommentData(commentId = commentId)
    }

    /**
     * 获取个人中心信息（头像、昵称、发帖/获赞/关注/粉丝等统计数据）
     *
     * @return Result<UserCenterInfo>
     */
    suspend fun getUserCenterInfo(): Result<UserCenterInfo> = runCatching {
        Log.d(TAG, "getUserCenterInfo: request")
        val response = api.getUserCenterInfo()
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    /**
     * 上传头像，返回 CDN URL。
     *
     * @param uri 本地图片 Uri（来自拍照或相册）
     * @return Result<String> 上传成功返回 CDN URL
     */
    suspend fun uploadAvatar(uri: Uri): Result<String> = runCatching {
        val context = com.vortexa.VortexaApplication.instance.applicationContext
        val file = uriToFile(context, uri) ?: throw IllegalArgumentException("无法读取图片文件")
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestFile)
        Log.d(TAG, "uploadAvatar: file=${file.name}")
        val response = api.uploadAvatar(part)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data?.url ?: throw ApiException(-1, "Upload response url is null")
    }

    /**
     * 编辑头像/昵称。
     *
     * @param userId 当前用户 ID，必填
     * @param avatar 新头像 URL，不修改传 null
     * @param userName 新昵称，不修改传 null
     * @return Result<UserCenterUpdateData>
     */
    suspend fun updateUserCenter(
        userId: Long,
        avatar: String? = null,
        userName: String? = null
    ): Result<UserCenterUpdateData> = runCatching {
        Log.d(TAG, "updateUserCenter: userId=$userId, avatar=${avatar != null}, userName=${userName != null}")
        val request = UserCenterUpdateRequest( avatar = avatar, userName = userName)
        val response = api.updateUserCenter(request)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    /**
     * 获取积分余额。
     *
     * @return Result<WalletPointData> 含 availablePoints 可用积分
     */
    suspend fun getWalletPoint(): Result<WalletPointData> = runCatching {
        Log.d(TAG, "getWalletPoint: request")
        val response = api.getWalletPoint()
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    /**
     * 获取指定用户发帖列表（分页）。
     *
     * @param userId 用户 ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     */
    suspend fun getUserCenterPosts(
        userId: Long,
        pageNum: Int = 1,
        pageSize: Int = 5
    ): Result<DynamicPostsResponse> = runCatching {
        Log.d(TAG, "getUserCenterPosts: userId=$userId pageNum=$pageNum pageSize=$pageSize")
        val response = api.getUserCenterPosts(userId = userId, pageNum = pageNum, pageSize = pageSize)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    /**
     * 获取指定用户回复列表（分页）。
     *
     * @param userId 用户 ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     */
    suspend fun getUserCenterComments(
        userId: Long,
        pageNum: Int = 1,
        pageSize: Int = 10
    ): Result<UserCenterCommentsResponse> = runCatching {
        Log.d(TAG, "getUserCenterComments: userId=$userId pageNum=$pageNum pageSize=$pageSize")
        val response = api.getUserCenterComments(userId = userId, pageNum = pageNum, pageSize = pageSize)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: throw ApiException(-1, "Response data is null")
    }

    /**
     * 删除帖子（逻辑删除，仅作者）。DELETE /v/api/user/posts/{postId}
     *
     * @param postId 帖子 ID
     * @return Result<DeletePostData>
     */
    /**
     * 编辑贴文（PUT /v/api/user/posts/update/{postId}，仅作者）。
     *
     * @param mediaListJson [UpdatePostRequest.mediaList]，已编码的 JSON 数组字符串或 null
     */
    suspend fun updatePost(
        postId: Long,
        module: String,
        title: String,
        content: String,
        mediaListJson: String? = null
    ): Result<Unit> = runCatching {
        Log.d(TAG, "updatePost: postId=$postId, module=$module")
        val response = api.updatePost(
            postId,
            UpdatePostRequest(
                module = module,
                title = title,
                content = content,
                mediaList = mediaListJson
            )
        )
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        Log.i(TAG, "updatePost: success, data=${response.data}")
    }

    suspend fun deletePost(postId: Long): Result<DeletePostData> = runCatching {
        Log.d(TAG, "deletePost: postId=$postId")
        val response = api.deletePost(postId)
        if (!response.isSuccess) {
            throw ApiException(response.code, response.message)
        }
        response.data ?: DeletePostData(msg = "删除成功")
    }

    /**
     * 将 content Uri 转为可读 File，用于上传。
     */
    private fun uriToFile(context: android.content.Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val cacheDir = context.cacheDir
            val file = File.createTempFile("avatar_upload_", ".jpg", cacheDir)
            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            file
        } catch (e: Exception) {
            Log.e(TAG, "uriToFile failed", e)
            null
        }
    }

    companion object {
        private const val TAG = "UserRepository"
    }
}
