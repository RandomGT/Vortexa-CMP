package com.vortexa.ui.page.post.detail

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.config.UserConfig
import com.vortexa.model.Post
import com.vortexa.ui.component.DeletePostConfirmModal
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.page.imagepreview.ImagePreviewActivity
import com.vortexa.ui.page.post.create.PostCreateActivity
import com.vortexa.ui.page.profile.other.OtherUserProfileActivity
import com.vortexa.ui.page.home.pager.home.recommend.PostImagesGrid
import com.vortexa.ui.page.post.detail.reply.CommentListView
import com.vortexa.ui.page.post.detail.reply.ReplyIndicatorBar
import com.vortexa.ui.theme.belowStatusBar
import com.vortexa.util.ImagePickValidator
import com.vortexa.util.ToastUtil
import java.io.File

/** 评论/回复输入框中图片数量上限（与发帖一致） */
private const val MAX_COMMENT_COMPOSER_IMAGES = 9

/**
 * 帖子详情页主视图
 * 通过 postId 调用 /v/api/home/posts/{postId} 加载详情，使用 PageStatusView 展示加载/失败状态；
 * 评论与回复由接口加载，ViewModel 映射为 [Comment]/[Reply]。
 * @param postId 贴文 ID，来自 Intent
 * @param editPayload 编辑态入参，存在时优先展示本地编辑内容
 * @param replyComposerHint 非 null 时详情加载成功后自动展开评论框并弹起键盘（预置回复对象）
 * @param openReplyComposerOnLoad 为 true 时（且无私信回复 hint）详情加载成功后展开底部回复区并弹起键盘，用于列表「评论」入口
 * @param onBack 顶部返回与删除成功后的返回回调
 */
@Composable
fun PostDetailView(
    postId: String,
    editPayload: PostDetailEditPayload? = null,
    replyComposerHint: PostDetailReplyComposerHint? = null,
    openReplyComposerOnLoad: Boolean = false,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: PostDetailViewModel = vortexaViewModel { PostDetailViewModel() }
) {
    val context = Context()
    val lifecycleOwner = LocalLifecycleOwner.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val pageStatus by viewModel.pageStatus.collectAsState()
    val detailData by viewModel.detailData.collectAsState()
    val commentList by viewModel.commentList.collectAsState()
    val hasMoreComments by viewModel.hasMoreComments.collectAsState()
    val commentLoadingMore by viewModel.commentLoadingMore.collectAsState()
    val replyTarget by viewModel.replyTarget.collectAsState()
    val followLoading by viewModel.followLoading.collectAsState()
    val unfollowLoading by viewModel.unfollowLoading.collectAsState()
    val replyLoading by viewModel.replyLoading.collectAsState()
    val commentsFilterUserId by viewModel.commentsFilterUserId.collectAsState()
    val deletePostLoading by viewModel.deletePostLoading.collectAsState()

    var composerState by remember { mutableStateOf<ComposerState>(ComposerState.Collapsed) }
    var inputValue by remember { mutableStateOf(TextFieldValue("")) }
    var selectedComposerMedia by remember { mutableStateOf<List<ComposerMediaPreview>>(emptyList()) }
    val latestComposerState by rememberUpdatedState(composerState)
    var appliedReplyComposerHint by remember(postId, replyComposerHint?.commentId) { mutableStateOf(false) }
    var appliedOpenReplyComposer by remember(postId, openReplyComposerOnLoad) { mutableStateOf(false) }
    var showDeletePostConfirm by remember { mutableStateOf(false) }

    val resolvedPostId = postId.ifBlank { "101" }

    // 多选图片（仅统计图片张数，最多 9 张；可与 1 个视频并存）
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = MAX_COMMENT_COMPOSER_IMAGES)
    ) { uris ->
        // 选图 Activity 返回后系统已收起键盘；从 Media 切回 Keyboard，触底栏再次 focus + show IME
        if (latestComposerState == ComposerState.Media) {
            composerState = ComposerState.Keyboard
        }
        if (uris.isEmpty()) {
            Log.d(TAG, "Image picker cancelled")
            return@rememberLauncherForActivityResult
        }
        val currentImageCount = selectedComposerMedia.count { !it.isVideo }
        val remaining = (MAX_COMMENT_COMPOSER_IMAGES - currentImageCount).coerceAtLeast(0)
        if (remaining == 0) return@rememberLauncherForActivityResult
        val capped = uris.take(remaining)
        val (validUris, firstReject) = ImagePickValidator.filterValidImageUris(context, capped)
        if (validUris.size < capped.size) {
            val skipped = capped.size - validUris.size
            val detail = firstReject?.let { ImagePickValidator.toastMessage(it) }.orEmpty()
            val msg = if (detail.isNotEmpty()) "已跳过 $skipped 张图片：$detail" else "已跳过 $skipped 张图片"
            ToastUtil.show(context, msg)
        }
        Log.d(TAG, "Images selected: picked=${capped.size}, accepted=${validUris.size}")
        val newItems = validUris.map { ComposerMediaPreview(uri = it, isVideo = false) }
        selectedComposerMedia = selectedComposerMedia + newItems
    }
    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (latestComposerState == ComposerState.Media) {
            composerState = ComposerState.Keyboard
        }
        if (uri == null) {
            Log.d(TAG, "Video picker cancelled")
            return@rememberLauncherForActivityResult
        }
        if (selectedComposerMedia.any { it.isVideo }) {
            ToastUtil.show(context, "最多添加 1 个视频")
            return@rememberLauncherForActivityResult
        }
        Log.d(TAG, "Video selected: $uri")
        selectedComposerMedia =
            selectedComposerMedia + ComposerMediaPreview(uri = uri, isVideo = true)
    }

    // 设置回复目标时进入键盘输入状态
    LaunchedEffect(replyTarget) {
        if (replyTarget != null) {
            Log.d(TAG, "Reply target changed, transition to Keyboard")
            composerState = ComposerState.Keyboard
        }
    }

    LaunchedEffect(composerState) {
        Log.d(TAG, "ComposerState changed: $composerState")
    }

    // 评论/回复发送成功后收起键盘、清空输入并折叠输入栏
    LaunchedEffect(Unit) {
        viewModel.commentSentSuccess.collect {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            inputValue = TextFieldValue("")
            selectedComposerMedia = emptyList()
            composerState = ComposerState.Collapsed
        }
    }

    LaunchedEffect(Unit) {
        viewModel.deletePostUi.collect { ev ->
            ToastUtil.show(context, ev.message)
            if (ev.success) {
                onBack()
            }
        }
    }

    // 每次 Activity onResume 拉取最新详情与评论（含首次进入：注册 Observer 时会收到当前 RESUMED 状态）
    DisposableEffect(lifecycleOwner, postId, editPayload) {
        if (editPayload != null) {
            Log.i(
                TAG,
                "Edit mode active, skip network load. postId=$postId, " +
                    "imageCount=${editPayload.imageResources.size}, videoCount=${editPayload.videoResources.size}"
            )
            return@DisposableEffect onDispose { }
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d(TAG, "ON_RESUME: refresh post detail")
                viewModel.refresh(resolvedPostId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 自个人主页「回复」等入口：详情成功后再拉评论与底栏就绪，预置回复目标并弹键盘
    LaunchedEffect(pageStatus, replyComposerHint, editPayload, appliedReplyComposerHint) {
        if (appliedReplyComposerHint || editPayload != null || replyComposerHint == null) return@LaunchedEffect
        if (pageStatus != PageStatus.Success) return@LaunchedEffect
        appliedReplyComposerHint = true
        viewModel.openReplyComposerForComment(
            commentId = replyComposerHint.commentId,
            authorName = replyComposerHint.authorName,
            content = replyComposerHint.commentSnippet,
            avatar = replyComposerHint.authorAvatar
        )
        Log.d(TAG, "Applied replyComposerHint, commentId=${replyComposerHint.commentId}")
    }

    // 列表「评论」等入口：仅打开发帖级回复输入区并弹键盘（无 ReplyIndicatorBar）
    LaunchedEffect(pageStatus, openReplyComposerOnLoad, editPayload, appliedOpenReplyComposer) {
        if (!openReplyComposerOnLoad || appliedOpenReplyComposer || editPayload != null) return@LaunchedEffect
        if (pageStatus != PageStatus.Success) return@LaunchedEffect
        appliedOpenReplyComposer = true
        viewModel.clearReplyTarget()
        composerState = ComposerState.Keyboard
        Log.d(TAG, "Applied openReplyComposerOnLoad (list comment entry)")
    }

    BackHandler(enabled = composerState != ComposerState.Collapsed) {
        Log.d(TAG, "Back pressed, collapse composer")
        composerState = ComposerState.Collapsed
        viewModel.clearReplyTarget()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .belowStatusBar()
            .imePadding()
    ) {
        // TitleBar：成功时有 data，失败时用占位避免 NPE；发帖人为自己时隐藏关注按钮
        val displayDetailData = mergeWithEditPayload(detailData, postId, editPayload)
        val post = displayDetailData?.post ?: Post(
            id = "", username = "", time = "", content = "",
            avatar = null, images = emptyList(), tagName = null
        )
        val isMyPost = post.userId != 0L && post.userId == UserConfig.getUserId()
        PostDetailTitleBar(
            post = post,
            showFollowButton = editPayload == null && !isMyPost,
            isFollowed = displayDetailData?.isFollowed ?: false,
            isCollect = displayDetailData?.isCollect ?: false,
            followLoading = followLoading,
            unfollowLoading = unfollowLoading,
            isMyPost = isMyPost,
            onBackClick = onBack,
            onFollowClick = { viewModel.follow(displayDetailData?.post?.userId ?: 0L) },
            onUnfollowConfirm = { viewModel.unfollow(displayDetailData?.post?.userId ?: 0L) },
            onBookmarkClick = { viewModel.toggleBookmark() },
            onMoreEditClick = {
                val d = displayDetailData ?: return@PostDetailTitleBar
                val raw = buildList {
                    for (item in d.post.images) {
                        when (item) {
                            is String -> if (item.isNotBlank()) add(item.trim())
                            is Uri -> item.toString().takeIf { s -> s.isNotBlank() }?.let { add(it) }
                        }
                    }
                }
                val (imgs, vids) = splitPostMediaUrls(raw)
                PostCreateActivity.startForEdit(
                    context = context,
                    postId = d.post.id,
                    title = d.title,
                    content = d.content,
                    board = d.post.module,
                    imageResources = imgs,
                    videoResources = vids
                )
            },
            onMoreDeleteClick = { showDeletePostConfirm = true },
            onlyTaFilterActive = commentsFilterUserId != null,
            onMoreOnlyTaClick = {
                if (commentsFilterUserId != null) viewModel.clearCommentsOnlyFilter()
                else viewModel.filterCommentsOnlyByUser(post.userId)
            },
            onAvatarClick = {
                OtherUserProfileActivity.startIfNotSelf(context, post.userId)
            }
        )

        Box(modifier = Modifier.weight(1f)) {
            CommentListView(
                modifier = Modifier.fillMaxSize(),
                comments = commentList,
                hasMore = hasMoreComments,
                loadingMore = commentLoadingMore,
                onLoadMore = { viewModel.loadMoreComments() },
                commentReplyTotal = displayDetailData?.commentCount,
                header = {
                    Column {
                        PostDetailBody(data = displayDetailData)
                        if (editPayload != null) {
                            val imageModels = editPayload.imageResources
                            val videoModels = editPayload.videoResources
                            if (imageModels.isNotEmpty()) {
                                PostImagesGrid(
                                    images = imageModels,
                                    onImageClick = { idx, urls -> ImagePreviewActivity.start(context, urls, idx) }
                                )
                            }
                            if (videoModels.isNotEmpty()) {
                                PostDetailVideoResourceList(videoResources = videoModels)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            )

            if (editPayload == null) {
                PageStatusView(
                    status = pageStatus,
                    modifier = Modifier.fillMaxSize(),
                    onRefresh = { viewModel.refresh(resolvedPostId) }
                )
            }

            // 输入区展开时：25% 黑色蒙层，点击收起键盘并折叠输入区（与系统返回一致）
            if (composerState != ComposerState.Collapsed) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            Log.d(TAG, "Overlay clicked, hide IME and collapse composer")
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            composerState = ComposerState.Collapsed
                            viewModel.clearReplyTarget()
                        }
                )
            } else {
                Box(Modifier.size(0.dp))
            }
        }

        // 回复指示条：当存在回复目标时显示
        replyTarget?.let { target ->
            ReplyIndicatorBar(
                target = target,
                onClose = {
                    viewModel.clearReplyTarget()
                }
            )
        }

        PostDetailBottomBar(
            modifier = Modifier,
            composerState = composerState,
            inputValue = inputValue,
            mediaPreviews = selectedComposerMedia,
            onValueChange = { inputValue = it },
            onComposerStateChange = { state ->
                Log.d(TAG, "ComposerState change request: $state")
                composerState = state
            },
            onSendClick = {
                Log.d(TAG, "Send click, trigger sendComment")
                viewModel.sendComment(
                    content = inputValue.text,
                    selectedMediaUris = selectedComposerMedia.map { it.uri.toString() }
                )
            },
            replyLoading = replyLoading,
            onPickImageClick = {
                val imageCount = selectedComposerMedia.count { !it.isVideo }
                if (imageCount >= MAX_COMMENT_COMPOSER_IMAGES) {
                    ToastUtil.show(context, "最多添加 9 张图片")
                    return@PostDetailBottomBar
                }
                Log.d(TAG, "Open image picker")
                pickImageLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onPickVideoClick = {
                Log.d(TAG, "Open video picker")
                composerState = ComposerState.Media
                pickVideoLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
            },
            onClearPreviewClick = {
                Log.d(TAG, "Clear selected media preview")
                selectedComposerMedia = emptyList()
            },
            onRemovePreviewAt = { index ->
                selectedComposerMedia =
                    selectedComposerMedia.filterIndexed { i, _ -> i != index }
            }
        )
    }

    if (showDeletePostConfirm) {
        DeletePostConfirmModal(
            onDismiss = { showDeletePostConfirm = false },
            onConfirm = { viewModel.deletePost() },
            deleteLoading = deletePostLoading
        )
    }
}

@Composable
private fun PostDetailViewPreview() {
    PostDetailView(postId = "101")
}

/** 按 URL 后缀粗略区分图片与视频，供跳转发帖编辑页传参。 */
private fun splitPostMediaUrls(urls: List<String>): Pair<List<String>, List<String>> {
    val videoExt = setOf("mp4", "webm", "mov", "m3u8", "mkv")
    val images = mutableListOf<String>()
    val videos = mutableListOf<String>()
    for (u in urls) {
        val s = u.trim()
        if (s.isEmpty()) continue
        val ext = s.substringAfterLast('.', "").lowercase()
        if (ext in videoExt) videos.add(s) else images.add(s)
    }
    return images to videos
}

/**
 * 将编辑态入参与接口详情合并，编辑态优先展示用户正在编辑的内容。
 * @param detailData 接口详情数据
 * @param postId 当前帖子 ID
 * @param editPayload 编辑态参数
 * @return 最终用于页面展示的详情数据
 */
private fun mergeWithEditPayload(
    detailData: PostDetailData?,
    postId: String,
    editPayload: PostDetailEditPayload?
): PostDetailData? {
    if (editPayload == null) return detailData
    val base = detailData ?: buildEditDetailData(postId, editPayload)
    return base.copy(
        title = editPayload.title,
        content = editPayload.content,
        contentFormat = null,
        post = base.post.copy(
            id = if (base.post.id.isNotBlank()) base.post.id else postId,
            title = editPayload.title,
            content = editPayload.content,
            summary = editPayload.content,
            images = editPayload.imageResources
        )
    )
}

/**
 * 在仅有编辑态入参、未请求详情接口时构造默认页面数据。
 * @param postId 帖子 ID
 * @param editPayload 编辑态参数
 * @return 仅用于编辑态展示的默认详情数据
 */
private fun buildEditDetailData(postId: String, editPayload: PostDetailEditPayload): PostDetailData {
    val normalizedPostId = postId.ifBlank { "0" }
    return PostDetailData(
        post = Post(
            id = normalizedPostId,
            username = "",
            avatar = null,
            time = "",
            content = editPayload.content,
            images = editPayload.imageResources,
            tagName = null,
            title = editPayload.title,
            summary = editPayload.content,
            publishTime = ""
        ),
        topicTag = "",
        title = editPayload.title,
        publishTime = "",
        content = editPayload.content,
        inlineTags = emptyList(),
        disclaimer = null,
        isFollowed = false,
        isCollect = false,
        likeCount = 0,
        collectCount = 0,
        commentCount = 0,
        isLiked = false
    )
}

private const val TAG = "PostDetailView"
