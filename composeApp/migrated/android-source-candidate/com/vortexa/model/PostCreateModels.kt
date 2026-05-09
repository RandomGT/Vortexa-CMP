package com.vortexa.model

/**
 * 发布贴文接口请求体（POST /v/api/home/discussion/post）。
 *
 * @param title 标题，必填
 * @param content 正文内容，支持文本、话题、@用户、表情、图片、视频、链接
 * @param module 发布板块，必填（如 综合、杂谈、交易经验、玩法）
 * @param mediaList 媒体列表，可选，JSON 字符串
 */
data class CreatePostRequest(
    val title: String,
    val content: String,
    val module: String,
    val mediaList: List<String>? = null
)

/**
 * 发布贴文接口响应 data。
 * 服务端当前返回对象而非纯数字，这里用宽松模型兼容解析。
 *
 * @param postId 新贴文 ID，可能为空
 * @param id 兼容部分接口直接返回 id 字段
 * @param status 贴文状态，可能为 reviewing/published 等
 */
data class CreatePostResponse(
    val postId: Long? = null,
    val id: Long? = null,
    val status: String? = null
)

/**
 * 编辑贴文请求体（PUT /v/api/user/posts/update/{postId}，仅作者）。
 *
 * @param module 发布板块
 * @param title 标题
 * @param content 正文
 * @param mediaList 多媒体列表，可选；接口字段类型为 string，传 JSON 数组字符串如 `["url1","url2"]`
 */
data class UpdatePostRequest(
    val module: String,
    val title: String,
    val content: String,
    val mediaList: String? = null
)

/** 将媒体 URL 列表编码为更新接口 [UpdatePostRequest.mediaList] 使用的 JSON 字符串；空列表返回 null。 */
fun mediaUrlListToJsonOrNull(urls: List<String>): String? {
    if (urls.isEmpty()) return null
    return urls.joinToString(prefix = "[", postfix = "]") { "\"${it.replace("\"", "\\\"")}\"" }
}
