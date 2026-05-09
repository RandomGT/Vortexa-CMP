package com.vortexa.ui.page.profile.paper.management

import android.util.Log
import com.vortexa.api.UserApi
import com.vortexa.lib_net.client.RetrofitClient
import com.vortexa.lib_net.extension.getDataOrThrow
import com.vortexa.model.UserPostItem
import com.vortexa.model.UserPostsRequest
import com.vortexa.model.UserPostsResponse

/**
 * 稿件管理 Repository。
 * 负责 POST /v/api/user/posts 接口调用与数据转换。
 */
class PaperManagementRepository {

    private val api: UserApi by lazy { RetrofitClient.createService() }

    companion object {
        private const val TAG = "PaperManagementRepository"
    }

    /**
     * 获取稿件管理列表。
     *
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @param status 0 全部，1 草稿箱，2 发布成功，3 未过审，4 审核中；null 表示全部
     * @param searchKeyword 关键字搜索
     * @param sortBy 排序方式
     * @return Result<UserPostsResponse>
     */
    suspend fun getPosts(
        pageNum: Int = 1,
        pageSize: Int = 20,
        status: Int? = null,
        searchKeyword: String? = null,
        sortBy: String = "newest"
    ): Result<UserPostsResponse> = runCatching {
        Log.d(TAG, "getPosts: pageNum=$pageNum, pageSize=$pageSize, status=$status")
        val request = UserPostsRequest(
            status = status,
            searchKeyword = searchKeyword,
            sortBy = sortBy
        )
        api.getPosts(pageNum = pageNum, pageSize = pageSize, request = request).getDataOrThrow()
    }

    /**
     * 将 UserPostItem 映射为 PaperItemData，供列表展示。
     * 接口字段可能为 null，统一用 orEmpty/默认值避免 NPE。
     */
    fun mapToPaperItemData(item: UserPostItem): PaperItemData = PaperItemData(
        postId = item.postId,
        board = item.module?.takeIf { it.isNotBlank() },
        avatarUrl = item.authorAvatar,
        name = (item.authorName ?: "").ifEmpty { "未知用户" },
        statusText = (item.statusText ?: "").ifEmpty { "未知" },
        dateText = firstNonBlankText(item.createdTime, item.createdAt, item.publishTime, item.updatedAt),
        title = item.title ?: "",
        description = item.summary ?: "",
        content = item.summary ?: "",
        likeCount = formatCount(item.likeCount),
        commentCount = formatCount(item.replyCount)
    )

    /**
     * 从多个候选字符串中取第一个非空白值。
     *
     * @param values 候选文本列表，允许为 null
     * @return 第一个非空白文本；若都为空则返回空字符串
     */
    private fun firstNonBlankText(vararg values: String?): String {
        return values.firstNotNullOfOrNull { value ->
            value?.takeIf { it.isNotBlank() }
        } ?: ""
    }

    private fun formatCount(count: Int): String = when {
        count >= 1_000_000 -> {
            val n = count / 1_000_000.0
            if (n == n.toInt().toDouble()) "${n.toInt()}M" else String.format("%.1fM", n)
        }
        count >= 1_000 -> {
            val n = count / 1_000.0
            if (n == n.toInt().toDouble()) "${n.toInt()}K" else String.format("%.1fK", n)
        }
        else -> count.toString()
    }
}
