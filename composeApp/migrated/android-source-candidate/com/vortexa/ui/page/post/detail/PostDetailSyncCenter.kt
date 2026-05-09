package com.vortexa.ui.page.post.detail

import android.util.Log
import com.vortexa.model.Post
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 帖子详情页对外同步的帖子变更事件。
 * @param postId 帖子 ID，用于命中外层列表项。
 * @param authorId 作者 ID，用于关注关系同步。
 * @param isLiked 最新点赞状态；null 表示本次未变更。
 * @param likeCount 最新点赞数；null 表示本次未变更。
 * @param isCollect 最新收藏状态；null 表示本次未变更。
 * @param collectCount 最新收藏数；null 表示本次未变更。
 * @param commentCount 最新评论数；null 表示本次未变更。
 * @param isFollowed 最新关注状态；null 表示本次未变更。
 * @param deleted true 时外层列表应移除该帖。
 */
data class PostDetailSyncEvent(
    val postId: String,
    val authorId: Long? = null,
    val isLiked: Boolean? = null,
    val likeCount: Int? = null,
    val isCollect: Boolean? = null,
    val collectCount: Int? = null,
    val commentCount: Int? = null,
    val isFollowed: Boolean? = null,
    val deleted: Boolean = false
)

/**
 * 帖子详情页与外层列表之间的同步中心。
 * 详情页分发事件，外层列表页订阅后局部刷新 UI。
 */
object PostDetailSyncCenter {

    private const val TAG = "PostDetailSyncCenter"

    private val _events = MutableSharedFlow<PostDetailSyncEvent>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** 外层列表订阅的帖子变更流。 */
    val events: SharedFlow<PostDetailSyncEvent> = _events.asSharedFlow()

    /**
     * 分发帖子详情页产生的变更事件。
     * @param event 本次需要同步到外层列表的帖子状态快照。
     * @return 无返回值。
     */
    fun dispatch(event: PostDetailSyncEvent) {
        val accepted = _events.tryEmit(event)
        if (accepted) {
            Log.d(
                TAG,
                "dispatch: postId=${event.postId}, deleted=${event.deleted}, liked=${event.isLiked}, " +
                    "collected=${event.isCollect}, comments=${event.commentCount}, followed=${event.isFollowed}"
            )
        } else {
            Log.w(TAG, "dispatch dropped: postId=${event.postId}")
        }
    }
}

/**
 * 将帖子详情页的最新状态合并到外层帖子列表。
 * @receiver 当前列表数据。
 * @param event 帖子详情页分发的同步事件。
 * @return 合并后的列表；若未命中帖子则返回原列表。
 */
fun List<Post>.applyPostDetailSync(event: PostDetailSyncEvent): List<Post> {
    if (event.deleted) {
        return filterNot { it.id == event.postId }
    }
    val index = indexOfFirst { it.id == event.postId }
    if (index == -1) {
        return this
    }
    val current = this[index]
    val updated = current.copy(
        isLiked = event.isLiked ?: current.isLiked,
        likeCount = event.likeCount ?: current.likeCount,
        isCollect = event.isCollect ?: current.isCollect,
        collectCount = event.collectCount ?: current.collectCount,
        commentCount = event.commentCount ?: current.commentCount
    )
    if (updated == current) {
        return this
    }
    return toMutableList().apply {
        set(index, updated)
    }
}
