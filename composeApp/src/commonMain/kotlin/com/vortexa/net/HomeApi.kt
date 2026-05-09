package com.vortexa.net

import com.vortexa.model.AuthorInfo
import com.vortexa.model.CommentReplyItem
import com.vortexa.model.CreatePostResponse
import com.vortexa.model.PostCommentItem
import com.vortexa.model.PostDetailResponse
import com.vortexa.model.PostImageUploadData
import com.vortexa.model.PostInfo
import com.vortexa.model.PostItem
import com.vortexa.model.RecommendCourseItem
import com.vortexa.model.RecommendCourseResponse
import com.vortexa.model.RecommendPostResponse
import com.vortexa.model.RecommendTeacherResponse
import com.vortexa.model.SearchSuggestResponse
import com.vortexa.model.TeacherItem
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

class HomeApi(
    private val client: ApiClient = ApiClient
) {
    suspend fun getRecommendPosts(pageNum: Int = 1, pageSize: Int = 3): RecommendPostResponse {
        val response = client.getJson(PATH_RECOMMEND_POST, pageQuery(pageNum, pageSize))
        return response.dataObject().toRecommendPostResponse(pageNum, pageSize)
    }

    suspend fun getRecommendCourse(
        pageNum: Int = 1,
        pageSize: Int = 4,
        userId: Long? = null
    ): RecommendCourseResponse {
        val response = client.getJson(
            PATH_RECOMMEND_COURSE,
            pageQuery(pageNum, pageSize) + mapOf("userId" to userId)
        )
        return response.dataObject().toRecommendCourseResponse(pageNum, pageSize)
    }

    suspend fun getRecommendTeachers(
        pageNum: Int = 1,
        pageSize: Int = 4,
        userId: Long? = null
    ): RecommendTeacherResponse {
        val response = client.getJson(
            PATH_RECOMMEND_TEACHER,
            pageQuery(pageNum, pageSize) + mapOf("userId" to userId)
        )
        return response.dataObject().toRecommendTeacherResponse(pageNum, pageSize)
    }

    suspend fun getDiscussionPosts(
        pageNum: Int = 1,
        pageSize: Int = 4,
        postType: Int
    ): RecommendPostResponse {
        val response = client.getJson(
            PATH_DISCUSSION_POST,
            pageQuery(pageNum, pageSize) + mapOf("postType" to postType)
        )
        return response.dataObject().toRecommendPostResponse(pageNum, pageSize)
    }

    suspend fun getSearchSuggest(): SearchSuggestResponse {
        val response = client.getJson(PATH_SEARCH_SUGGEST)
        return response.dataObject().toSearchSuggestResponse()
    }

    suspend fun getPostDetail(postId: Long): PostDetailResponse {
        val response = client.getJson("$PATH_POSTS/$postId")
        return response.dataObject().toPostDetailResponse()
    }

    suspend fun getPostComments(
        postId: Long,
        pageNum: Int = 1,
        pageSize: Int = 5,
        userId: Long? = null
    ): List<PostCommentItem> {
        val response = client.getJson(
            "$PATH_POSTS/$postId/comment",
            pageQuery(pageNum, pageSize) + mapOf("userId" to userId)
        )
        return response.dataObject().jsonArray("list").map { it.asObject().toPostCommentItem() }
    }

    suspend fun getCommentReplies(
        commentId: Long,
        pageNum: Int = 1,
        pageSize: Int = 5
    ): List<CommentReplyItem> {
        val response = client.getJson("$PATH_COMMENTS/$commentId/replies", pageQuery(pageNum, pageSize))
        return response.dataObject().jsonArray("list").map { it.asObject().toCommentReplyItem() }
    }

    suspend fun createDiscussionPost(
        title: String,
        content: String,
        module: String,
        mediaList: List<String>? = null
    ): CreatePostResponse {
        val response = client.postJson(
            PATH_CREATE_POST,
            buildJsonObject {
                put("title", title)
                put("content", content)
                put("module", module)
                putStringListIfNotNull("mediaList", mediaList)
            }
        )
        return (response.data as? JsonObject)?.toCreatePostResponse() ?: CreatePostResponse()
    }

    suspend fun postComment(
        postId: Long,
        parentCommentId: Long?,
        content: String,
        mediaList: List<String>? = null
    ) {
        client.postJson(
            PATH_POST_COMMENT,
            buildJsonObject {
                put("postId", postId)
                if (parentCommentId != null) put("parentCommentId", parentCommentId)
                put("content", content)
                putStringListIfNotNull("mediaList", mediaList)
            }
        )
    }

    companion object {
        private const val PATH_RECOMMEND_POST = "v/api/home/recommend/post"
        private const val PATH_RECOMMEND_COURSE = "v/api/home/course"
        private const val PATH_RECOMMEND_TEACHER = "v/api/home/teacher"
        private const val PATH_DISCUSSION_POST = "v/api/home/discussion/post"
        private const val PATH_SEARCH_SUGGEST = "v/api/home/search/suggest"
        private const val PATH_POSTS = "v/api/home/posts"
        private const val PATH_COMMENTS = "v/api/home/comments"
        private const val PATH_CREATE_POST = "v/api/home/post/insert"
        private const val PATH_POST_COMMENT = "v/api/home/discussion/comments"
    }
}

private fun pageQuery(pageNum: Int, pageSize: Int): Map<String, Any?> =
    mapOf("pageNum" to pageNum, "pageSize" to pageSize)

private fun ApiResponse.dataObject(): JsonObject =
    data as? JsonObject ?: throw ApiException(-1, "Response data is null")

private fun JsonElement.asObject(): JsonObject =
    this as? JsonObject ?: JsonObject(emptyMap())

private fun JsonObject.jsonArray(key: String): List<JsonElement> =
    (this[key] as? JsonArray)?.toList().orEmpty()

private fun JsonObject.long(key: String): Long? =
    (this[key] as? JsonPrimitive)?.longOrNull

private fun JsonObject.int(key: String): Int? =
    (this[key] as? JsonPrimitive)?.intOrNull

private fun JsonObject.float(key: String): Float? =
    (this[key] as? JsonPrimitive)?.content?.toFloatOrNull()

private fun JsonObject.boolean(key: String): Boolean? =
    (this[key] as? JsonPrimitive)?.booleanOrNull

private fun JsonObject.stringList(key: String): List<String>? {
    val array = this[key] as? JsonArray ?: return null
    return array.mapNotNull { (it as? JsonPrimitive)?.content }
}

private fun JsonObject.toRecommendPostResponse(defaultPageNum: Int, defaultPageSize: Int): RecommendPostResponse =
    RecommendPostResponse(
        total = int("total") ?: jsonArray("list").size,
        pageNum = int("pageNum") ?: defaultPageNum,
        pageSize = int("pageSize") ?: defaultPageSize,
        list = jsonArray("list").map { it.asObject().toPostItem() }
    )

private fun JsonObject.toPostItem(): PostItem = PostItem(
    postId = long("postId") ?: long("id") ?: 0L,
    userId = long("userId") ?: 0L,
    nickname = stringValue("nickname") ?: stringValue("userName") ?: "",
    avatar = stringValue("avatar"),
    title = stringValue("title"),
    summary = stringValue("summary") ?: stringValue("content"),
    mediaList = stringList("mediaList"),
    totalMediaCount = int("totalMediaCount") ?: stringList("mediaList")?.size ?: 0,
    module = stringValue("module") ?: stringValue("board"),
    isInteractionHot = boolean("isInteractionHot") ?: false,
    isViewHot = boolean("isViewHot") ?: false,
    likeCount = int("likeCount") ?: 0,
    collectCount = int("collectCount") ?: 0,
    replyCount = int("replyCount") ?: int("commentCount") ?: 0,
    isLiked = boolean("isLiked") ?: false,
    isCollect = boolean("isCollect") ?: false,
    publishTime = stringValue("publishTime")
)

private fun JsonObject.toRecommendCourseResponse(defaultPageNum: Int, defaultPageSize: Int): RecommendCourseResponse =
    RecommendCourseResponse(
        pageNum = int("pageNum") ?: defaultPageNum,
        pageSize = int("pageSize") ?: defaultPageSize,
        total = int("total") ?: jsonArray("list").size,
        list = jsonArray("list").map { it.asObject().toRecommendCourseItem() }
    )

private fun JsonObject.toRecommendCourseItem(): RecommendCourseItem = RecommendCourseItem(
    courseId = long("courseId") ?: long("id") ?: 0L,
    cover = stringValue("cover"),
    title = stringValue("title") ?: "",
    authorId = long("authorId"),
    avatar = stringValue("avatar"),
    authorNickname = stringValue("authorNickname") ?: stringValue("nickname"),
    studentCount = int("studentCount") ?: 0
)

private fun JsonObject.toRecommendTeacherResponse(defaultPageNum: Int, defaultPageSize: Int): RecommendTeacherResponse =
    RecommendTeacherResponse(
        total = int("total") ?: jsonArray("list").size,
        pageNum = int("pageNum") ?: defaultPageNum,
        pageSize = int("pageSize") ?: defaultPageSize,
        list = jsonArray("list").map { it.asObject().toTeacherItem() }
    )

private fun JsonObject.toTeacherItem(): TeacherItem = TeacherItem(
    teacherId = long("teacherId") ?: long("userId") ?: long("id") ?: 0L,
    avatar = stringValue("avatar"),
    nickname = stringValue("nickname") ?: stringValue("teacherName") ?: "",
    tags = stringList("tags") ?: stringList("tagList"),
    price = float("price") ?: 0f,
    score = stringValue("score") ?: float("score")?.toString().orEmpty()
)

private fun JsonObject.toSearchSuggestResponse(): SearchSuggestResponse =
    SearchSuggestResponse(suggestions = stringList("suggestions").orEmpty())

private fun JsonObject.toPostDetailResponse(): PostDetailResponse = PostDetailResponse(
    authorInfo = (this["authorInfo"] as? JsonObject)?.toAuthorInfo()
        ?: AuthorInfo(0L, null, "", false),
    postInfo = (this["postInfo"] as? JsonObject)?.toPostInfo()
        ?: PostInfo(0L, null, null, null, null, 0, 0, 0, false, null)
)

private fun JsonObject.toAuthorInfo(): AuthorInfo = AuthorInfo(
    authorId = long("authorId") ?: long("userId") ?: 0L,
    authorAvatar = stringValue("authorAvatar") ?: stringValue("avatar"),
    authorName = stringValue("authorName") ?: stringValue("nickname") ?: "",
    isFollowed = boolean("isFollowed") ?: false
)

private fun JsonObject.toPostInfo(): PostInfo = PostInfo(
    postId = long("postId") ?: long("id") ?: 0L,
    title = stringValue("title"),
    content = stringValue("content") ?: stringValue("summary"),
    module = stringValue("module"),
    board = stringValue("board"),
    likeCount = int("likeCount") ?: 0,
    collectCount = int("collectCount") ?: 0,
    replyCount = int("replyCount") ?: 0,
    isCollect = boolean("isCollect") ?: false,
    publishTime = stringValue("publishTime"),
    mediaList = stringList("mediaList"),
    totalMediaCount = int("totalMediaCount")
)

private fun JsonObject.toPostCommentItem(): PostCommentItem = PostCommentItem(
    commentId = long("commentId") ?: long("id") ?: 0L,
    postId = long("postId") ?: 0L,
    parentCommentId = long("parentCommentId"),
    userId = long("userId") ?: 0L,
    userAvatar = stringValue("userAvatar") ?: stringValue("avatar"),
    userName = stringValue("userName") ?: stringValue("nickname") ?: "",
    content = stringValue("content") ?: "",
    likeCount = int("likeCount") ?: 0,
    publishTime = stringValue("publishTime") ?: "",
    isAuthor = boolean("isAuthor") ?: false,
    isLiked = boolean("isLiked") ?: false,
    mediaList = stringList("mediaList")
)

private fun JsonObject.toCommentReplyItem(): CommentReplyItem = CommentReplyItem(
    commentId = long("commentId") ?: long("id") ?: 0L,
    postId = long("postId") ?: 0L,
    parentCommentId = long("parentCommentId") ?: 0L,
    replyToUserId = long("replyToUserId"),
    userId = long("userId") ?: 0L,
    userAvatar = stringValue("userAvatar") ?: stringValue("avatar"),
    userName = stringValue("userName") ?: stringValue("nickname") ?: "",
    content = stringValue("content") ?: "",
    likeCount = int("likeCount") ?: 0,
    isAuthor = boolean("isAuthor") ?: false,
    publishTime = stringValue("publishTime") ?: "",
    isLiked = boolean("isLiked") ?: false,
    mediaList = stringList("mediaList")
)

private fun JsonObject.toCreatePostResponse(): CreatePostResponse = CreatePostResponse(
    postId = long("postId"),
    id = long("id"),
    status = stringValue("status")
)

private fun JsonObjectBuilder.putStringListIfNotNull(key: String, values: List<String>?) {
    if (values != null) {
        put(key, buildJsonArray {
            values.forEach { add(JsonPrimitive(it)) }
        })
    }
}
