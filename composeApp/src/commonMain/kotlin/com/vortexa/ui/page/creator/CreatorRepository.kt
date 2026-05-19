package com.vortexa.ui.page.creator

import android.util.Log
import com.vortexa.config.UserConfig
import com.vortexa.model.CreatorActivity
import com.vortexa.model.CreatorActivityListResponse
import com.vortexa.model.CreatorCertification
import com.vortexa.model.CreatorData
import com.vortexa.model.CreatorPostDataPage
import com.vortexa.model.CreatorStatisticsPostItem
import com.vortexa.model.CreatorTask
import com.vortexa.model.CreatorTaskListResponse
import com.vortexa.model.CreatorUserInfo
import com.vortexa.net.ApiClient
import com.vortexa.net.ApiException
import com.vortexa.net.ApiResponse
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

/**
 * 创作中心 commonMain 仓库。
 * 优先读取真实接口；P2 阶段后端不可用时回落到本地假数据，保证页面可完整走通。
 */
class CreatorRepository(
    private val client: ApiClient = ApiClient,
    private val fallbackToMock: Boolean = true,
) {
    suspend fun getCreatorData(days: Int = 7): Result<CreatorData> =
        requestWithFallback(
            label = "getCreatorData",
            block = { client.getJson("v/api/user/creator/data/$days").dataObject().toCreatorData(days) },
            fallback = { mockCreatorData(days) },
        )

    suspend fun getCreatorUserInfo(): Result<CreatorUserInfo> =
        requestWithFallback(
            label = "getCreatorUserInfo",
            block = {
                val userId = UserConfig.getUserId()
                client.getJson("v/api/user/profile/$userId").dataObject().toCreatorUserInfo()
            },
            fallback = { mockCreatorUserInfo() },
        )

    suspend fun getCreatorActivities(
        pageNum: Int = 1,
        pageSize: Int = 4,
    ): Result<CreatorActivityListResponse> =
        requestWithFallback(
            label = "getCreatorActivities",
            block = {
                client.getJson(
                    path = "v/api/user/creator/activities",
                    query = mapOf("pageNum" to pageNum, "pageSize" to pageSize),
                ).dataObject().toCreatorActivityListResponse(pageNum, pageSize)
            },
            fallback = {
                val list = mockCreatorActivities().take(pageSize)
                CreatorActivityListResponse(page = pageNum, pageSize = pageSize, total = list.size, list = list)
            },
        )

    suspend fun getCreatorTasks(
        pageNum: Int = 1,
        pageSize: Int = 3,
    ): Result<CreatorTaskListResponse> =
        requestWithFallback(
            label = "getCreatorTasks",
            block = {
                client.getJson(
                    path = "v/api/user/creator/tasks",
                    query = mapOf("pageNum" to pageNum, "pageSize" to pageSize),
                ).dataObject().toCreatorTaskListResponse(pageNum, pageSize)
            },
            fallback = {
                val list = mockCreatorTasks().take(pageSize)
                CreatorTaskListResponse(pageNum = pageNum, pageSize = pageSize, total = list.size, list = list)
            },
        )

    suspend fun getPostDataList(
        days: Int = 7,
        pageNum: Int = 1,
        pageSize: Int = 20,
        sortBy: Int = 0,
    ): Result<CreatorPostDataPage> =
        requestWithFallback(
            label = "getPostDataList",
            block = {
                client.getJson(
                    path = "v/api/user/posts/data/$days",
                    query = mapOf("pageNum" to pageNum, "pageSize" to pageSize, "sortBy" to sortBy),
                ).dataObject().toCreatorPostDataPage(pageNum, pageSize)
            },
            fallback = { mockPostDataPage(pageNum, pageSize, sortBy) },
        )

    private suspend fun <T> requestWithFallback(
        label: String,
        block: suspend () -> T,
        fallback: () -> T,
    ): Result<T> =
        runCatching { block() }
            .recoverCatching { error ->
                if (!fallbackToMock) throw error
                Log.w(TAG, "$label failed, use mock data", error)
                fallback()
            }

    private fun JsonElement.asObject(): JsonObject =
        this as? JsonObject ?: JsonObject(emptyMap())

    private fun ApiResponse.dataObject(): JsonObject =
        data as? JsonObject ?: throw ApiException(-1, "Response data is null")

    private fun JsonObject.array(key: String): List<JsonElement> =
        (this[key] as? JsonArray)?.toList().orEmpty()

    private fun JsonObject.string(key: String): String? =
        (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content

    private fun JsonObject.stringOrNumber(key: String): String? =
        (this[key] as? JsonPrimitive)?.content

    private fun JsonObject.int(key: String): Int? =
        (this[key] as? JsonPrimitive)?.intOrNull

    private fun JsonObject.long(key: String): Long? =
        (this[key] as? JsonPrimitive)?.longOrNull ?: string(key)?.toLongOrNull()

    private fun JsonObject.boolean(key: String): Boolean? =
        (this[key] as? JsonPrimitive)?.booleanOrNull

    private fun JsonObject.toCreatorData(defaultDays: Int): CreatorData = CreatorData(
        userId = long("userId") ?: UserConfig.getUserId(),
        days = int("days") ?: defaultDays,
        postCount = int("postCount") ?: 0,
        viewCount = int("viewCount") ?: 0,
        likeCount = int("likeCount") ?: 0,
        commentCount = int("commentCount") ?: int("replyCount") ?: 0,
        followerGrowth = int("followerGrowth") ?: 0,
        pageVisitors = int("pageVisitors") ?: int("visitorCount") ?: 0,
        shares = int("shares") ?: int("shareCount") ?: 0,
        revenue = int("revenue") ?: 0,
    )

    private fun JsonObject.toCreatorUserInfo(): CreatorUserInfo {
        val userInfo = (this["userInfo"] as? JsonObject) ?: this
        return CreatorUserInfo(
            userId = userInfo.long("userId") ?: userInfo.long("id") ?: UserConfig.getUserId(),
            userAvatar = userInfo.string("avatar") ?: userInfo.string("userAvatar"),
            userName = userInfo.string("nickname") ?: userInfo.string("userName") ?: UserConfig.getNickname(),
            certifications = array("certifications").map { it.asObject().toCreatorCertification() },
            isFollowed = boolean("isFollowed") ?: false,
            followCount = userInfo.int("followCount") ?: 0,
            fanCount = userInfo.int("fanCount") ?: userInfo.int("followerCount") ?: 0,
            intro = userInfo.string("intro"),
            isVerified = userInfo.boolean("isVerified") ?: false,
        )
    }

    private fun JsonObject.toCreatorCertification(): CreatorCertification = CreatorCertification(
        type = string("type").orEmpty(),
        name = string("name").orEmpty(),
    )

    private fun JsonObject.toCreatorActivityListResponse(
        defaultPage: Int,
        defaultPageSize: Int,
    ): CreatorActivityListResponse {
        val list = array("list").map { it.asObject().toCreatorActivity() }
        return CreatorActivityListResponse(
            page = int("page") ?: int("pageNum") ?: defaultPage,
            pageSize = int("pageSize") ?: defaultPageSize,
            total = int("total") ?: list.size,
            list = list,
        )
    }

    private fun JsonObject.toCreatorActivity(): CreatorActivity = CreatorActivity(
        id = long("id") ?: 0L,
        title = string("title").orEmpty(),
        cover = string("cover"),
        startAt = string("startAt") ?: string("startTime"),
        endAt = string("endAt") ?: string("endTime"),
        status = string("status"),
        detailUrl = string("detailUrl") ?: string("url"),
    )

    private fun JsonObject.toCreatorTaskListResponse(
        defaultPage: Int,
        defaultPageSize: Int,
    ): CreatorTaskListResponse {
        val list = array("list").map { it.asObject().toCreatorTask() }
        return CreatorTaskListResponse(
            pageNum = int("pageNum") ?: int("page") ?: defaultPage,
            pageSize = int("pageSize") ?: defaultPageSize,
            total = int("total") ?: list.size,
            list = list,
        )
    }

    private fun JsonObject.toCreatorTask(): CreatorTask = CreatorTask(
        id = long("id") ?: 0L,
        title = string("title").orEmpty(),
        description = string("description"),
        status = string("status"),
        progress = int("progress") ?: 0,
        target = int("target") ?: 0,
        reward = string("reward"),
        rewardAmount = int("rewardAmount"),
        canClaim = boolean("canClaim") ?: string("status").equals("completed", ignoreCase = true),
        completedAt = string("completedAt"),
        claimedAt = string("claimedAt"),
    )

    private fun JsonObject.toCreatorPostDataPage(
        defaultPage: Int,
        defaultPageSize: Int,
    ): CreatorPostDataPage {
        val rows = array("postList").ifEmpty { array("list") }
        return CreatorPostDataPage(
            list = rows.map { it.asObject().toCreatorStatisticsPostItem() },
            total = int("total") ?: rows.size,
        )
    }

    private fun JsonObject.toCreatorStatisticsPostItem(): CreatorStatisticsPostItem =
        CreatorStatisticsPostItem(
            postId = long("postId") ?: stringOrNumber("postId")?.toLongOrNull() ?: 0L,
            nickname = string("authorName") ?: string("nickname") ?: "",
            avatar = string("authorAvatar") ?: string("avatar"),
            publishTime = string("publishTime") ?: string("createTime"),
            title = string("title"),
            summary = string("summary") ?: string("content"),
            viewCount = long("viewCount") ?: 0L,
            likeCount = int("likeCount") ?: 0,
            replyCount = int("replyCount") ?: int("commentCount") ?: 0,
            shareCount = int("shareCount") ?: int("shares") ?: 0,
            revenue = int("revenue") ?: 0,
        )

    private fun mockCreatorData(days: Int): CreatorData = CreatorData(
        userId = UserConfig.getUserId(),
        days = days,
        postCount = if (days <= 1) 1 else 8,
        viewCount = 250 + days * 86,
        likeCount = 20 + days * 3,
        commentCount = 33 + days,
        followerGrowth = 1 + days / 7,
        pageVisitors = 180 + days * 22,
        shares = 12 + days,
        revenue = 0,
    )

    private fun mockCreatorUserInfo(): CreatorUserInfo = CreatorUserInfo(
        userId = UserConfig.getUserId(),
        userAvatar = UserConfig.getAvatar(),
        userName = UserConfig.getNickname()?.takeIf { it.isNotBlank() } ?: "Capper",
        certifications = listOf(
            CreatorCertification(type = "creator", name = "认证导师"),
            CreatorCertification(type = "v", name = "大V"),
        ),
        isFollowed = false,
        followCount = 26,
        fanCount = 1280,
        intro = "持续输出优质内容",
        isVerified = true,
    )

    private fun mockCreatorActivities(): List<CreatorActivity> = listOf(
        CreatorActivity(101, "春节创作激励活动", null, "2026-01-20", "2026-02-10", "ongoing", "/activities/101"),
        CreatorActivity(102, "新作者成长计划", null, "2026-02-01", "2026-02-28", "upcoming", "/activities/102"),
        CreatorActivity(103, "优质答主招募", null, "2026-03-01", "2026-03-31", "ongoing", "/activities/103"),
    )

    private fun mockCreatorTasks(): List<CreatorTask> = listOf(
        CreatorTask(1, "发布首篇内容", "成功发布 1 篇内容", "completed", 1, 1, "创作积分", 100, true, null, null),
        CreatorTask(2, "7 日连续创作", "连续 7 天发布内容", "unfinished", 4, 7, "创作积分", 300, false, null, null),
        CreatorTask(3, "单篇内容浏览破 1,000", "任意一篇内容浏览量达到 1000", "claimed", 1000, 1000, "现金券", 10, false, null, null),
        CreatorTask(4, "发布1篇帖子并获得200浏览", null, "completed", 200, 200, "创作积分", 50, true, null, null),
    )

    private fun mockPostDataPage(pageNum: Int, pageSize: Int, sortBy: Int): CreatorPostDataPage {
        val total = 36
        val start = (pageNum - 1) * pageSize
        val rows = (start until minOf(start + pageSize, total)).map { index ->
            CreatorStatisticsPostItem(
                postId = (index + 1).toLong(),
                nickname = UserConfig.getNickname()?.takeIf { it.isNotBlank() } ?: "Kaelani Silvermoon",
                avatar = UserConfig.getAvatar(),
                publishTime = "2026-05-${(19 - index % 10).toString().padStart(2, '0')} 09:${(index * 7 % 60).toString().padStart(2, '0')}:23",
                title = listOf("关于比特币：难忘的瞬间", "链上趋势观察", "创作者复盘笔记")[index % 3],
                summary = "关于比特币、区块链和加密货币趋势的最新见解。",
                viewCount = 163000L - index * 1400L,
                likeCount = 163000 - index * 830,
                replyCount = 12000 - index * 34,
                shareCount = 1200 - index * 5,
                revenue = 0,
            )
        }
        val sorted = when (sortBy) {
            1 -> rows.sortedByDescending { it.viewCount }
            2 -> rows.sortedByDescending { it.replyCount }
            3 -> rows.sortedByDescending { it.likeCount }
            else -> rows
        }
        return CreatorPostDataPage(list = sorted, total = total)
    }

    companion object {
        private const val TAG = "CreatorRepository"
    }
}
