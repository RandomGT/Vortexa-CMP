package com.vortexa.model

/**
 * 互动管理接口请求体（POST /v/api/user/interactions）。
 * 分页参数 pageNum、pageSize 通过 URL Query 传递，不放在 Body 中。
 *
 * @param actorType 互动对象类型：0=所有人，1=我的关注，2=关注我的，3=陌生人
 * @param actionType 互动类型：0=点赞，1=回复
 * @param direction 互动方向：0=全部，1=我发起的，2=被互动的
 */
data class InteractionRequest(
    val actorType: Int = 0,
    val actionType: Int = 0,
    val direction: Int = 0
)

/**
 * 互动管理接口响应体。
 */
data class InteractionResponse(
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val list: List<InteractionListItem>
)

/**
 * 互动列表单条。
 *
 * @param userId 用户 ID
 * @param userName 用户昵称
 * @param userAvatar 头像 URL
 * @param action 互动类型：0=点赞，1=回复
 * @param type 目标类型：1=贴文，2=评论
 * @param typeData 关联内容摘要（贴文标题/评论内容）
 * @param time 时间
 * @param postId 关联帖子 ID，用于点击跳转 PostDetail，0 表示无关联
 */
data class InteractionListItem(
    val userId: Long,
    val userName: String,
    val userAvatar: String = "",
    val action: Int,
    val type: Int,
    val typeData: String = "",
    val time: String = "",
    val postId: Long = 0L
)
