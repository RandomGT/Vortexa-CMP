package com.vortexa.ui.page.post.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.model.CommentReplyItem
import com.vortexa.model.Post
import com.vortexa.model.PostCommentItem
import com.vortexa.model.PostDetailResponse
import com.vortexa.ui.page.post.detail.reply.Comment
import com.vortexa.ui.page.post.detail.reply.Reply
import com.vortexa.ui.page.post.detail.reply.ReplyTarget
import com.vortexa.repository.HomeRepository
import com.vortexa.repository.UserRepository
import com.vortexa.ui.component.pageStatus.PageStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 贴文详情页 ViewModel。
 * 负责加载贴文详情（/v/api/home/posts/{postId}）、页面状态及点赞/收藏/关注交互。
 */
class PostDetailViewModel(
    private val homeRepository: HomeRepository = HomeRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _pageStatus = MutableStateFlow<PageStatus>(PageStatus.Loading)
    /** 页面请求状态，供 [PageStatusView] 使用 */
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    private val _detailData = MutableStateFlow<PostDetailData?>(null)
    /** 贴文详情数据，Success 时供 UI 展示 */
    val detailData: StateFlow<PostDetailData?> = _detailData.asStateFlow()

    private val _commentList = MutableStateFlow<List<Comment>>(emptyList())
    /** 一级评论列表，供 [CommentListView] 展示 */
    val commentList: StateFlow<List<Comment>> = _commentList.asStateFlow()

    private val _commentPageNum = MutableStateFlow(1)
    private val _hasMoreComments = MutableStateFlow(false)
    private val _commentLoadingMore = MutableStateFlow(false)
    /** 是否还有更多评论可加载 */
    val hasMoreComments: StateFlow<Boolean> = _hasMoreComments.asStateFlow()
    /** 上拉加载更多中 */
    val commentLoadingMore: StateFlow<Boolean> = _commentLoadingMore.asStateFlow()

    /** 「只看 TA」时传入评论接口的 userId；null 表示全部评论 */
    private val _commentsFilterUserId = MutableStateFlow<Long?>(null)
    /** 浏览他人帖子时，是否处于「只看 TA」筛选（用于右上角菜单文案：只看TA / 取消只看TA） */
    val commentsFilterUserId: StateFlow<Long?> = _commentsFilterUserId.asStateFlow()

    private val _replyTarget = MutableStateFlow<ReplyTarget?>(null)
    /** 当前回复目标，非 null 时在底部输入栏上方显示回复指示条 */
    val replyTarget: StateFlow<ReplyTarget?> = _replyTarget.asStateFlow()

    private val _followLoading = MutableStateFlow(false)
    /** 关注请求加载态 */
    val followLoading: StateFlow<Boolean> = _followLoading.asStateFlow()

    private val _unfollowLoading = MutableStateFlow(false)
    /** 取消关注请求加载态 */
    val unfollowLoading: StateFlow<Boolean> = _unfollowLoading.asStateFlow()

    private val _replyLoading = MutableStateFlow(false)
    /** 回复/评论发送请求加载态，供底部发送按钮 LoadingButton 使用 */
    val replyLoading: StateFlow<Boolean> = _replyLoading.asStateFlow()

    private val _deletePostLoading = MutableStateFlow(false)
    /** 删除帖子请求加载态，供删除确认弹窗确定按钮使用 */
    val deletePostLoading: StateFlow<Boolean> = _deletePostLoading.asStateFlow()

    private val _commentSentSuccess = MutableSharedFlow<Unit>()
    /** 评论/回复发送成功事件，UI 收到后清空输入并收起输入栏 */
    val commentSentSuccess: SharedFlow<Unit> = _commentSentSuccess

    private val _deletePostUi = MutableSharedFlow<DeletePostUiEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    /** 删除帖子结果：成功时关页前 Toast，失败仅 Toast */
    val deletePostUi: SharedFlow<DeletePostUiEvent> = _deletePostUi.asSharedFlow()

    /**
     * 加载贴文详情（全屏 Loading）。
     * @param postId 贴文 ID，需为有效数字字符串，否则不发起请求
     */
    fun loadDetail(postId: String) {
        fetchPostDetail(postId, allowSilent = false)
    }

    /**
     * 重新拉取详情；若当前已在 Success 且有数据（例如从编辑页/个人页返回），则不切全屏 Loading、失败时保留原界面。
     */
    fun refresh(postId: String) {
        fetchPostDetail(postId, allowSilent = true)
    }

    private fun fetchPostDetail(postId: String, allowSilent: Boolean) {
        val id = postId.toLongOrNull()
        if (id == null) {
            Log.w(TAG, "fetchPostDetail: invalid postId=$postId, skip")
            _pageStatus.value = PageStatus.Fail
            return
        }
        val silent =
            allowSilent &&
                _pageStatus.value == PageStatus.Success &&
                _detailData.value != null
        viewModelScope.launch {
            if (!silent) {
                _pageStatus.value = PageStatus.Loading
            }
            Log.d(TAG, "fetchPostDetail: postId=$id silent=$silent")
            homeRepository.getPostDetail(id)
                .onSuccess { response ->
                    _detailData.value = mapToDetailData(response)
                    _pageStatus.value = PageStatus.Success
                    Log.d(TAG, "fetchPostDetail: success")
                    loadComments(id)
                }
                .onFailure {
                    if (!silent) {
                        _pageStatus.value = PageStatus.Fail
                    }
                    Log.e(TAG, "fetchPostDetail: failed", it)
                }
        }
    }

    /**
     * 加载帖子一级评论及回复，详情加载成功后调用。
     * 评论加载成功后并行请求每条评论的回复并合并。
     * @param postId 贴文 ID
     * @param pageNum 页码，1 为首页（替换列表），>1 为加载更多（追加）
     * @param userId 可选，「只看TA」时传入
     */
    fun loadComments(postId: Long, pageNum: Int = 1, userId: Long? = null) {
        val effectiveUserId = userId ?: _commentsFilterUserId.value
        if (pageNum > 1 && _commentLoadingMore.value) {
            Log.d(TAG, "loadComments: already loading more, skip")
            return
        }
        if (pageNum > 1) {
            _commentLoadingMore.value = true
        }
        viewModelScope.launch {
            try {
                Log.d(TAG, "loadComments: postId=$postId pageNum=$pageNum")
                val pageSize = 10
                homeRepository.getPostComments(postId, pageNum = pageNum, pageSize = pageSize, userId = effectiveUserId)
                    .onSuccess { items ->
                        val postAuthorId = _detailData.value?.post?.userId ?: 0L
                        val commentsWithEmptyReplies = items.map { mapToComment(it, postAuthorId) }
                        val merged = loadRepliesForComments(commentsWithEmptyReplies, items, postAuthorId)
                        applyCommentsResult(pageNum, merged, items.size, pageSize)
                        Log.d(TAG, "loadComments: success, size=${items.size}")
                    }
                    .onFailure {
                        Log.e(TAG, "loadComments: failed", it)
                        if (pageNum == 1) {
                            _commentList.value = emptyList()
                            _hasMoreComments.value = false
                        }
                    }
            } finally {
                _commentLoadingMore.value = false
            }
        }
    }

    /**
     * 应用评论加载结果，合并或替换列表，更新分页状态。
     * @param pageNum 当前页码
     * @param merged 本页评论（含回复）
     * @param fetchedSize 本页实际条数
     * @param pageSize 每页大小
     */
    private fun applyCommentsResult(pageNum: Int, merged: List<Comment>, fetchedSize: Int, pageSize: Int) {
        _commentPageNum.value = pageNum
        _hasMoreComments.value = fetchedSize >= pageSize
        _commentList.value = if (pageNum == 1) merged else (_commentList.value + merged)
    }

    /**
     * 上拉加载更多评论，由 [CommentListView] 接近底部时触发。
     */
    fun loadMoreComments() {
        val postId = _detailData.value?.post?.id?.toLongOrNull() ?: return
        if (!_hasMoreComments.value || _commentLoadingMore.value) return
        loadComments(postId, pageNum = _commentPageNum.value + 1)
    }

    /**
     * 仅展示指定用户的评论：重新请求第一页评论并带上 [userId]。
     */
    fun filterCommentsOnlyByUser(userId: Long) {
        val postId = _detailData.value?.post?.id?.toLongOrNull() ?: return
        _commentsFilterUserId.value = userId
        loadComments(postId, pageNum = 1)
    }

    /** 取消「只看 TA」，重新加载全部评论 */
    fun clearCommentsOnlyFilter() {
        val postId = _detailData.value?.post?.id?.toLongOrNull() ?: return
        if (_commentsFilterUserId.value == null) return
        _commentsFilterUserId.value = null
        loadComments(postId, pageNum = 1)
    }

    /** 删除当前帖子（DELETE /v/api/user/posts/{postId}），成功则同步外层列表并发出 [deletePostUi]。 */
    fun deletePost() {
        val postIdStr = _detailData.value?.post?.id ?: return
        val postIdLong = postIdStr.toLongOrNull() ?: run {
            Log.w(TAG, "deletePost: invalid postId=$postIdStr")
            return
        }
        viewModelScope.launch {
            _deletePostLoading.value = true
            userRepository.deletePost(postIdLong)
                .onSuccess { data ->
                    PostDetailSyncCenter.dispatch(
                        PostDetailSyncEvent(postId = postIdStr, deleted = true)
                    )
                    val msg = data.msg?.trim()?.takeIf { it.isNotEmpty() } ?: "删除成功"
                    _deletePostUi.emit(DeletePostUiEvent(success = true, message = msg))
                    Log.d(TAG, "deletePost: success postId=$postIdStr")
                }
                .onFailure { e ->
                    val msg = e.message?.trim()?.takeIf { it.isNotEmpty() } ?: "删除失败，请稍后重试"
                    _deletePostUi.emit(DeletePostUiEvent(success = false, message = msg))
                    Log.e(TAG, "deletePost: failed postId=$postIdStr", e)
                }
            _deletePostLoading.value = false
        }
    }

    /**
     * 并行加载每条评论的回复并合并。
     * @param comments 已加载的一级评论（replies 为空）
     * @param commentItems 原始接口数据，用于获取评论者 userId（replyToName 解析）
     * @return 带回复的评论列表
     */
    private suspend fun loadRepliesForComments(
        comments: List<Comment>,
        commentItems: List<PostCommentItem>,
        postAuthorId: Long,
    ): List<Comment> {
        if (comments.isEmpty()) return emptyList()
        val parentMap = commentItems.associate { it.commentId.toString() to (it.userId to it.userName) }
        val commentIdToReplyState = coroutineScope {
            comments.map { comment ->
                async {
                    val commentId = comment.id.toLongOrNull() ?: return@async comment.id to ReplyLoadState(emptyList(), false, 1)
                    val (parentUserId, parentAuthorName) = parentMap[comment.id] ?: (0L to comment.authorName)
                    homeRepository.getCommentReplies(
                        commentId,
                        pageNum = 1,
                        pageSize = REPLY_PAGE_SIZE
                    )
                        .getOrNull()
                        ?.let { replyItems ->
                            val mapped = mapReplyItemsToReplies(
                                replyItems,
                                parentUserId,
                                parentAuthorName,
                                existingReplies = emptyList(),
                                postAuthorId = postAuthorId,
                            )
                            val hasMore = replyItems.size >= REPLY_PAGE_SIZE
                            val nextPage = if (hasMore) 2 else 1
                            comment.id to ReplyLoadState(mapped, hasMore, nextPage)
                        } ?: (comment.id to ReplyLoadState(emptyList(), false, 1))
                }
            }.awaitAll()
        }
        val stateMap = commentIdToReplyState.toMap()
        val merged = comments.map { c ->
            val state = stateMap[c.id] ?: ReplyLoadState(emptyList(), false, 1)
            c.copy(
                replies = state.replies,
                hasMoreReplies = state.hasMore,
                nextReplyPage = state.nextPage,
                repliesLoadingMore = false
            )
        }
        Log.d(TAG, "loadRepliesForComments: done, totalReplies=${merged.sumOf { it.replies.size }}")
        return merged
    }

    /**
     * 加载某条一级评论的下一页回复并追加到列表末尾。
     */
    fun loadMoreReplies(commentId: String) {
        val idLong = commentId.toLongOrNull() ?: return
        val current = _commentList.value.firstOrNull { it.id == commentId } ?: return
        if (!current.hasMoreReplies || current.repliesLoadingMore) return
        viewModelScope.launch {
            updateCommentById(commentId) { it.copy(repliesLoadingMore = true) }
            try {
                val pageNum = _commentList.value.firstOrNull { it.id == commentId }?.nextReplyPage ?: return@launch
                homeRepository.getCommentReplies(
                    idLong,
                    pageNum = pageNum,
                    pageSize = REPLY_PAGE_SIZE
                )
                    .onSuccess { replyItems ->
                        val postAuthorId = _detailData.value?.post?.userId ?: 0L
                        updateCommentById(commentId) { prev ->
                            if (replyItems.isEmpty()) {
                                return@updateCommentById prev.copy(hasMoreReplies = false, repliesLoadingMore = false)
                            }
                            val newReplies = mapReplyItemsToReplies(
                                replyItems,
                                prev.userId,
                                prev.authorName,
                                existingReplies = prev.replies,
                                postAuthorId = postAuthorId,
                            )
                            val existingIds = prev.replies.map { it.id }.toSet()
                            val appended = newReplies.filter { it.id !in existingIds }
                            val hasMore = replyItems.size >= REPLY_PAGE_SIZE
                            prev.copy(
                                replies = prev.replies + appended,
                                hasMoreReplies = hasMore,
                                nextReplyPage = if (hasMore) pageNum + 1 else prev.nextReplyPage,
                                repliesLoadingMore = false
                            )
                        }
                        Log.d(TAG, "loadMoreReplies: commentId=$commentId page=$pageNum size=${replyItems.size}")
                    }
                    .onFailure { e ->
                        updateCommentById(commentId) { it.copy(repliesLoadingMore = false) }
                        Log.e(TAG, "loadMoreReplies: commentId=$commentId failed", e)
                    }
            } catch (e: Exception) {
                updateCommentById(commentId) { it.copy(repliesLoadingMore = false) }
                Log.e(TAG, "loadMoreReplies: commentId=$commentId error", e)
            }
        }
    }

    private fun updateCommentById(commentId: String, transform: (Comment) -> Comment) {
        val list = _commentList.value
        val idx = list.indexOfFirst { it.id == commentId }
        if (idx < 0) return
        val next = list.toMutableList().apply { this[idx] = transform(this[idx]) }
        _commentList.value = next
    }

    private fun mapReplyItemsToReplies(
        replyItems: List<CommentReplyItem>,
        parentUserId: Long,
        parentAuthorName: String,
        existingReplies: List<Reply>,
        postAuthorId: Long,
    ): List<Reply> {
        val userIdToName = mutableMapOf<Long, String>().apply {
            put(parentUserId, parentAuthorName)
            existingReplies.forEach { put(it.userId, it.authorName) }
            replyItems.forEach { put(it.userId, it.userName) }
        }
        return replyItems.map { mapToReply(it, parentAuthorName, userIdToName, postAuthorId) }
    }

    /** 接口 [isAuthor] 与发帖人 userId 任一命中即视为楼主展示 */
    private fun resolveIsPostAuthor(isAuthorFlag: Boolean, userId: Long, postAuthorId: Long): Boolean =
        isAuthorFlag || (postAuthorId != 0L && userId == postAuthorId)

    private data class ReplyLoadState(
        val replies: List<Reply>,
        val hasMore: Boolean,
        val nextPage: Int
    )

    private fun mapToComment(item: PostCommentItem, postAuthorId: Long): Comment = Comment(
        id = item.commentId.toString(),
        authorName = item.userName,
        avatar = item.userAvatar,
        userId = item.userId,
        isAuthor = resolveIsPostAuthor(item.isAuthor, item.userId, postAuthorId),
        content = item.content,
        images = item.mediaList.orEmpty().filter { it.isNotBlank() },
        likeCount = item.likeCount,
        time = item.publishTime,
        isLiked = item.isLiked,
        replies = emptyList()
    )

    /**
     * 将回复接口数据转为 [Reply]。
     * @param item 接口单条
     * @param parentAuthorName 评论作者名，replyToUserId 为 null 时使用
     * @param userIdToName 已加载回复中的 userId->userName，用于 replyToUserId 查找
     */
    private fun mapToReply(
        item: CommentReplyItem,
        parentAuthorName: String,
        userIdToName: Map<Long, String>,
        postAuthorId: Long,
    ): Reply = Reply(
        id = item.commentId.toString(),
        authorName = item.userName,
        avatar = item.userAvatar,
        userId = item.userId,
        isAuthor = resolveIsPostAuthor(item.isAuthor, item.userId, postAuthorId),
        replyToName = when (val id = item.replyToUserId) {
            null -> parentAuthorName
            else -> userIdToName[id] ?: "用户"
        },
        content = item.content,
        images = item.mediaList.orEmpty().filter { it.isNotBlank() },
        likeCount = item.likeCount,
        isLiked = item.isLiked,
        time = item.publishTime
    )

    /** 切换收藏状态，调用收藏/取消收藏接口 */
    fun toggleBookmark() {
        val data = _detailData.value ?: return
        val postIdLong = data.post.id.toLongOrNull() ?: return
        viewModelScope.launch {
            val result = if (data.isCollect) userRepository.uncollectPost(postIdLong) else userRepository.collectPost(postIdLong)
            result
                .onSuccess {
                    val current = _detailData.value ?: return@onSuccess
                    val updated = current.copy(
                        post = current.post.copy(
                            isCollect = !current.isCollect,
                            collectCount = (current.collectCount + if (current.isCollect) -1 else 1).coerceAtLeast(0)
                        ),
                        isCollect = !current.isCollect,
                        collectCount = (current.collectCount + if (current.isCollect) -1 else 1).coerceAtLeast(0)
                    )
                    _detailData.value = updated
                    dispatchPostSync(updated)
                    Log.d(TAG, "toggleBookmark: postId=${data.post.id} success")
                }
                .onFailure { Log.e(TAG, "toggleBookmark: postId=${data.post.id} failed", it) }
        }
    }

    /** 切换点赞状态，调用点赞/取消点赞接口 */
    fun toggleLike() {
        val data = _detailData.value ?: return
        val postIdLong = data.post.id.toLongOrNull() ?: return
        viewModelScope.launch {
            val result = if (data.isLiked) userRepository.unlikePost(postIdLong) else userRepository.likePost(postIdLong)
            result
                .onSuccess {
                    val current = _detailData.value ?: return@onSuccess
                    val updated = current.copy(
                        post = current.post.copy(
                            isLiked = !current.isLiked,
                            likeCount = (current.likeCount + if (current.isLiked) -1 else 1).coerceAtLeast(0)
                        ),
                        isLiked = !current.isLiked,
                        likeCount = (current.likeCount + if (current.isLiked) -1 else 1).coerceAtLeast(0)
                    )
                    _detailData.value = updated
                    dispatchPostSync(updated)
                    Log.d(TAG, "toggleLike: postId=${data.post.id} success")
                }
                .onFailure {
                    Log.e(TAG, "toggleLike: postId=${data.post.id} failed", it)
                }
        }
    }

    /**
     * 关注目标用户。
     * @param userId 目标用户 ID，通常为 post.authorInfo.authorId
     */
    fun follow(userId: Long) {
        viewModelScope.launch {
            _followLoading.value = true
            Log.d(TAG, "follow: userId=$userId")
            userRepository.follow(userId)
                .onSuccess {
                    val data = _detailData.value ?: return@onSuccess
                    if (data.post.userId == userId) {
                        val updated = data.copy(isFollowed = true)
                        _detailData.value = updated
                        dispatchFollowSync(updated.post.id, userId, true)
                    }
                    Log.d(TAG, "follow: success")
                }
                .onFailure {
                    Log.e(TAG, "follow: failed", it)
                }
            _followLoading.value = false
        }
    }

    /**
     * 取消关注目标用户。
     * @param userId 目标用户 ID
     */
    fun unfollow(userId: Long) {
        viewModelScope.launch {
            _unfollowLoading.value = true
            Log.d(TAG, "unfollow: userId=$userId")
            userRepository.unfollow(userId)
                .onSuccess {
                    val data = _detailData.value ?: return@onSuccess
                    if (data.post.userId == userId) {
                        val updated = data.copy(isFollowed = false)
                        _detailData.value = updated
                        dispatchFollowSync(updated.post.id, userId, false)
                    }
                    Log.d(TAG, "unfollow: success")
                }
                .onFailure {
                    Log.e(TAG, "unfollow: failed", it)
                }
            _unfollowLoading.value = false
        }
    }

    /**
     * 开始回复一级评论，设置回复目标并触发键盘弹起
     */
    fun startReplyToComment(comment: Comment) {
        _replyTarget.value = ReplyTarget(
            id = comment.id,
            authorName = comment.authorName,
            avatar = comment.avatar,
            content = comment.content,
            isComment = true
        )
    }

    /**
     * 从外链（如个人主页「回复」列表）进入详情时，用接口/列表外的元数据预置回复目标并弹起输入区。
     */
    fun openReplyComposerForComment(
        commentId: Long,
        authorName: String,
        content: String,
        avatar: Any? = null
    ) {
        _replyTarget.value = ReplyTarget(
            id = commentId.toString(),
            authorName = authorName,
            avatar = avatar,
            content = content,
            isComment = true
        )
    }

    /**
     * 开始回复二级回复，设置回复目标并触发键盘弹起。
     * @param reply 被回复的回复
     * @param rootCommentId 所在楼层的一级评论 ID，用于接口 parentCommentId
     */
    fun startReplyToReply(reply: Reply, rootCommentId: String) {
        _replyTarget.value = ReplyTarget(
            id = reply.id,
            authorName = reply.authorName,
            avatar = reply.avatar,
            content = reply.content,
            isComment = false,
            rootCommentId = rootCommentId
        )
    }

    /** 清除回复目标，隐藏回复指示条 */
    fun clearReplyTarget() {
        _replyTarget.value = null
    }

    /**
     * 发送评论/回复。
     * @param content 正文，允许为空（仅发图时）
     * @param selectedMediaUris 选中的图片/视频 Uri 列表，为空时不上传媒体
     * @return 成功时已清除回复目标并刷新评论列表
     */
    fun sendComment(content: String, selectedMediaUris: List<String> = emptyList()) {
        val trimmed = content.trim()
        val postId = _detailData.value?.post?.id?.toLongOrNull()
        if (postId == null) {
            Log.w(TAG, "sendComment: postId null, skip")
            return
        }
        if (trimmed.isBlank() && selectedMediaUris.isEmpty()) {
            Log.w(TAG, "sendComment: content and media both empty, skip")
            return
        }
        val target = _replyTarget.value
        val parentCommentId: Long? = when {
            target == null -> null
            target.isComment -> target.id.toLongOrNull()
            else -> target.rootCommentId?.toLongOrNull()
        }
        viewModelScope.launch {
            _replyLoading.value = true
            Log.i(TAG, "sendComment: start, postId=$postId, parentCommentId=$parentCommentId")
            try {
                val finalMediaList = selectedMediaUris.ifEmpty { null }
                homeRepository.postComment(
                    postId = postId,
                    parentCommentId = parentCommentId,
                    content = trimmed.ifBlank { "" },
                    mediaList = finalMediaList
                )
                    .onSuccess {
                        val current = _detailData.value
                        if (current != null) {
                            val updated = current.copy(
                                post = current.post.copy(commentCount = current.post.commentCount + 1),
                                commentCount = current.post.commentCount + 1
                            )
                            _detailData.value = updated
                            dispatchPostSync(updated)
                        }
                        clearReplyTarget()
                        loadComments(postId)
                        _commentSentSuccess.emit(Unit)
                        Log.d(TAG, "sendComment: success")
                    }
                    .onFailure {
                        Log.e(TAG, "sendComment: failed", it)
                    }
            } finally {
                _replyLoading.value = false
            }
        }
    }

    /**
     * 回复点赞/取消点赞，调用 /v/api/user/like/comment/{commentId}。
     * 成功后更新该回复的 likeCount 与 isLiked，并刷新评论列表状态。
     */
    fun onReplyLikeClick(reply: Reply) {
        val commentIdLong = reply.id.toLongOrNull() ?: return
        viewModelScope.launch {
            Log.d(TAG, "onReplyLikeClick: replyId=${reply.id}, isLiked=${reply.isLiked}")
            val result = if (reply.isLiked) userRepository.unlikeComment(commentIdLong) else userRepository.likeComment(commentIdLong)
            result
                .onSuccess {
                    val list = _commentList.value.toMutableList()
                    var updated = false
                    val merged = list.map { comment ->
                        val idx = comment.replies.indexOfFirst { it.id == reply.id }
                        if (idx != -1) {
                            updated = true
                            val r = comment.replies[idx]
                            comment.copy(
                                replies = comment.replies.toMutableList().apply {
                                    set(idx, r.copy(
                                        isLiked = !r.isLiked,
                                        likeCount = (r.likeCount + if (r.isLiked) -1 else 1).coerceAtLeast(0)
                                    ))
                                }
                            )
                        } else comment
                    }
                    if (updated) _commentList.value = merged
                    Log.d(TAG, "onReplyLikeClick: replyId=${reply.id} success")
                }
                .onFailure { Log.e(TAG, "onReplyLikeClick: replyId=${reply.id} failed", it) }
        }
    }

    /**
     * 切换评论点赞状态，调用点赞/取消点赞评论接口。
     */
    fun onLikeClick(comment: Comment) {
        val commentIdLong = comment.id.toLongOrNull() ?: return
        viewModelScope.launch {
            val result = if (comment.isLiked) userRepository.unlikeComment(commentIdLong) else userRepository.likeComment(commentIdLong)
            result
                .onSuccess {
                    val list = _commentList.value.toMutableList()
                    val idx = list.indexOfFirst { it.id == comment.id }
                    if (idx != -1) {
                        val c = list[idx]
                        list[idx] = c.copy(
                            isLiked = !c.isLiked,
                            likeCount = (c.likeCount + if (c.isLiked) -1 else 1).coerceAtLeast(0)
                        )
                        _commentList.value = list
                    }
                    Log.d(TAG, "onLikeClick: commentId=${comment.id} success")
                }
                .onFailure { Log.e(TAG, "onLikeClick: commentId=${comment.id} failed", it) }
        }
    }

    /**
     * 将详情页中的帖子交互状态同步到外层列表。
     * @param data 当前详情页最新帖子状态。
     * @return 无返回值。
     */
    private fun dispatchPostSync(data: PostDetailData) {
        PostDetailSyncCenter.dispatch(
            PostDetailSyncEvent(
                postId = data.post.id,
                authorId = data.post.userId,
                isLiked = data.isLiked,
                likeCount = data.likeCount,
                isCollect = data.isCollect,
                collectCount = data.collectCount,
                commentCount = data.commentCount
            )
        )
    }

    /**
     * 将详情页中的关注状态同步到外层列表。
     * @param postId 当前帖子 ID。
     * @param authorId 当前作者 ID。
     * @param isFollowed 最新关注状态。
     * @return 无返回值。
     */
    private fun dispatchFollowSync(postId: String, authorId: Long, isFollowed: Boolean) {
        PostDetailSyncCenter.dispatch(
            PostDetailSyncEvent(
                postId = postId,
                authorId = authorId,
                isFollowed = isFollowed
            )
        )
    }

    private fun mapToDetailData(response: PostDetailResponse): PostDetailData {
        val author = response.authorInfo
        val info = response.postInfo
        val topicSection = info.module?.takeIf { it.isNotBlank() } ?: info.board?.takeIf { it.isNotBlank() }
        return PostDetailData(
            post = Post(
                id = info.postId.toString(),
                username = author.authorName,
                avatar = author.authorAvatar,
                time = info.publishTime ?: "",
                content = info.content ?: "",
                images = info.mediaList.orEmpty().filter { it.isNotBlank() },
                tagName = topicSection,
                likeCount = info.likeCount,
                commentCount = info.replyCount,
                isLiked = false,
                isCollect = info.isCollect,
                userId = author.authorId,
                title = info.title,
                summary = info.content,
                totalMediaCount = info.totalMediaCount ?: info.mediaList.orEmpty().size,
                module = topicSection,
                collectCount = info.collectCount,
                publishTime = info.publishTime
            ),
            topicTag = topicSection?.let { "#$it" } ?: "",
            title = info.title ?: "",
            publishTime = info.publishTime ?: "",
            content = info.content ?: "",
            inlineTags = emptyList(),
            disclaimer = null,
            isFollowed = author.isFollowed,
            isCollect = info.isCollect,
            likeCount = info.likeCount,
            collectCount = info.collectCount,
            commentCount = info.replyCount,
            isLiked = false
        )
    }

    private companion object {
        const val TAG = "PostDetailVM"
        /** 每条评论回复列表每页条数（首屏与加载更多一致；不少于该条数时认为可能还有更多） */
        const val REPLY_PAGE_SIZE = 10
    }
}

/** 删除帖子 UI 反馈（Toast 文案；success 时 Activity 可 finish） */
data class DeletePostUiEvent(val success: Boolean, val message: String)

/**
 * 贴文详情页使用的统一数据结构。
 * 包含 [Post]（供 TitleBar 等复用）、主体展示字段及交互状态。
 */
data class PostDetailData(
    val post: Post,
    val topicTag: String,
    val title: String,
    val publishTime: String,
    val content: String,
    val inlineTags: List<String>,
    val disclaimer: String?,
    val isFollowed: Boolean,
    val isCollect: Boolean,
    val likeCount: Int,
    val collectCount: Int,
    val commentCount: Int,
    val isLiked: Boolean
)
