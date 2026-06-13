package com.vortexa.ui.page.post.create

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.vortexa.ui.component.AppLoadingIndicator
import com.vortexa.ui.component.LoadingIndicatorSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.platform.MediaPicker
import com.vortexa.platform.MediaType
import com.vortexa.platform.PickedMedia
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.ui.page.post.detail.ComposerMediaPreview
import com.vortexa.ui.page.post.detail.ComposerState
import com.vortexa.ui.page.post.detail.PostDetailBottomBar
import com.vortexa.ui.page.post.detail.PostInlineTopicVisualTransformation
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.ui.theme.FontSemiBold
import com.vortexa.ui.theme.belowStatusBar
import com.vortexa.util.ImagePickValidator
import com.vortexa.util.ToastUtil
import kotlinx.coroutines.launch

private const val TITLE_MAX_LENGTH = 30

/** 发帖正文附件中图片上限（与评论/回复一致） */
private const val MAX_POST_CREATE_IMAGES = 9

/** 正文与话题区整体视口最大高度（正文在区内滚动，话题 Chip 固定在区底） */
private val POST_CREATE_BODY_SCROLL_MAX_HEIGHT = 360.dp

/**
 * 发布贴文页主视图（Figma 278-24772）。
 * 顶部栏、标题输入、正文输入（可滚）、话题 Chip 固定在正文区底部；点击话题在正文插入 `#话题名`（仅前导 `#`）。
 */
@Composable
fun PostCreateView(
    modifier: Modifier = Modifier,
    viewModel: PostCreateViewModel = vortexaViewModel { PostCreateViewModel() },
    editArgs: PostCreateEditArgs? = null,
    onPublishSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val topicSuggestions by viewModel.topicSuggestions.collectAsState()
    val selectedModuleIndex by viewModel.selectedModuleIndex.collectAsState()
    val isPublishing by viewModel.isPublishing.collectAsState()
    val publishError by viewModel.publishError.collectAsState()
    val publishSuccess by viewModel.publishSuccess.collectAsState()

    var contentValue by remember { mutableStateOf(TextFieldValue(content)) }
    var composerState by remember { mutableStateOf<ComposerState>(ComposerState.Collapsed) }
    var isTitleFocused by remember { mutableStateOf(false) }
    var isContentFocused by remember { mutableStateOf(false) }
    val selectedMediaList = remember { androidx.compose.runtime.mutableStateListOf<PostCreateSelectedMedia>() }
    val density = LocalDensity.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val contentFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    val imeVisible = WindowInsets.ime.getBottom(density) > 0
    // 工具条模式无底栏「收起占位」，仅在编辑标题时隐藏，否则保留图/视频等入口
    val shouldShowBottomBar = !isTitleFocused

    LaunchedEffect(editArgs) {
        val args = editArgs
            ?: (context as? PostCreateActivity)?.let { PostCreateActivity.parseEditArgs(it.intent) }
            ?: return@LaunchedEffect
        viewModel.applyEditDraft(
            postId = args.postId,
            title = args.title,
            content = args.content,
            board = args.board
        )
        contentValue = TextFieldValue(args.content)
        selectedMediaList.clear()
        for (url in args.imageResources) {
            val s = url.trim()
            if (s.isEmpty()) continue
            selectedMediaList.add(
                PostCreateSelectedMedia(
                    uri = Uri.parse(s),
                    type = PostCreateMediaType.Image,
                    isRemote = true
                )
            )
        }
        for (url in args.videoResources) {
            val s = url.trim()
            if (s.isEmpty()) continue
            selectedMediaList.add(
                PostCreateSelectedMedia(
                    uri = Uri.parse(s),
                    type = PostCreateMediaType.Video,
                    isRemote = true
                )
            )
        }
    }

    LaunchedEffect(publishSuccess) {
        if (publishSuccess) {
            val act = context as? Activity
            // 通知启动方（如交流页）发帖成功，便于返回后刷新列表
            act?.setResult(Activity.RESULT_OK)
            Log.i(TAG, "publish success, set RESULT_OK and finish")
            act?.finish()
            onPublishSuccess()
        }
    }
    LaunchedEffect(publishError) {
        val msg = publishError?.trim().orEmpty()
        if (msg.isEmpty()) return@LaunchedEffect
        val toastText = when {
            msg == POST_CREATE_ERR_TITLE_EMPTY || msg == POST_CREATE_ERR_BODY_EMPTY -> msg
            msg.contains("暂不支持上传本地视频") -> msg
            else -> "发布失败，请稍后重试"
        }
        ToastUtil.show(context, toastText)
        viewModel.clearPublishError()
    }
    LaunchedEffect(isContentFocused, imeVisible, isTitleFocused) {
        if (isTitleFocused) {
            composerState = ComposerState.Collapsed
            return@LaunchedEffect
        }
        if (isContentFocused && imeVisible) {
            composerState = ComposerState.Keyboard
        } else if (!isContentFocused && !imeVisible && composerState == ComposerState.Keyboard) {
            composerState = ComposerState.Collapsed
        }
    }

    LaunchedEffect(composerState, isTitleFocused) {
        when {
            composerState == ComposerState.Keyboard && !isTitleFocused -> {
                withFrameNanos { }
                contentFocusRequester.requestFocus()
                keyboardController?.show()
            }
            composerState == ComposerState.Media -> {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        }
    }

    /** 发帖请求进行中：收起键盘，避免与全屏遮罩叠压 */
    LaunchedEffect(isPublishing) {
        if (isPublishing) {
            keyboardController?.hide()
            Log.i(TAG, "publishing: hide keyboard, show blocking overlay")
        }
    }

    /** 接口未完成前消费系统返回，防止用户误退导致重复提交或状态错乱 */
    BackHandler(enabled = isPublishing) {
        Log.d(TAG, "back ignored while publishing")
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .belowStatusBar()
            .background(Color.White)
            .imePadding()
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                PostCreateTopBar(
                    selectedModuleIndex = selectedModuleIndex,
                    onModuleSelect = { viewModel.selectModule(it) },
                    onPublishClick = {
                        viewModel.publish(selectedMedia = selectedMediaList.toList())
                    },
                    isPublishing = isPublishing
                )

                // 标题区（Figma 278-24782）— 固定，不参与正文滚动
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, start = 18.dp, end = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BasicTextField(
                            value = title,
                            onValueChange = { viewModel.updateTitle(it) },
                            textStyle = FontSemiBold(fontSize = 18, color = Colors.black_101828),
                            singleLine = true,
                            visualTransformation = VisualTransformation.None,
                            cursorBrush = SolidColor(Colors.blue_3266FF),
                            decorationBox = { inner ->
                                Box(modifier = Modifier.weight(1f)) {
                                    if (title.isEmpty()) {
                                        Text(
                                            text = "请输入标题（必填）",
                                            style = FontSemiBold(fontSize = 18, color = Colors.gray_B1B8C6)
                                        )
                                    }
                                    inner()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { focusState ->
                                    isTitleFocused = focusState.isFocused
                                }
                        )
                        Text(
                            text = "${title.length}/$TITLE_MAX_LENGTH",
                            style = FontRegular(fontSize = 14, color = Colors.gray_B1B8C6)
                        )
                    }
                    Spacer(modifier = Modifier.height(7.dp))
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 0.5.dp,
                        color = Colors.gray_EBEBEB
                    )
                }

                val bodyScrollState = rememberScrollState()
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val sectionHeight =
                        minOf(maxHeight, POST_CREATE_BODY_SCROLL_MAX_HEIGHT)
                    Column(
                        modifier = Modifier
                            .height(sectionHeight)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(bodyScrollState)
                        ) {
                            // 正文区（Figma 278-24787）— 仅正文滚动，话题 Chip 固定在下方
                            BasicTextField(
                                value = contentValue,
                                onValueChange = {
                                    contentValue = it
                                    viewModel.updateContent(it.text)
                                },
                                visualTransformation = PostInlineTopicVisualTransformation,
                                textStyle = FontRegular(fontSize = 18, color = Colors.black_101828),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 18.dp, bottom = 10.dp, start = 18.dp, end = 18.dp)
                                    .focusRequester(contentFocusRequester)
                                    .onFocusChanged { focusState ->
                                        isContentFocused = focusState.isFocused
                                    },
                                minLines = 6,
                                cursorBrush = SolidColor(Colors.blue_3266FF),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                                decorationBox = { inner ->
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        if (contentValue.text.isEmpty()) {
                                            Text(
                                                text = "请输入正文（必填）",
                                                style = FontRegular(fontSize = 18, color = Colors.gray_B1B8C6)
                                            )
                                        }
                                        inner()
                                    }
                                }
                            )
                        }

                        PostCreateTopicChips(
                            topics = topicSuggestions,
                            onTopicClick = { topic ->
                                val (newText, newCursor) = viewModel.insertTopicToken(
                                    text = contentValue.text,
                                    selectionStart = contentValue.selection.min,
                                    selectionEnd = contentValue.selection.max,
                                    topic = topic
                                )
                                contentValue = TextFieldValue(newText, TextRange(newCursor))
                                viewModel.updateContent(newText)
                            },
                            onCustomTopicClick = {
                                val (newText, newCursor) = viewModel.insertCustomTopicTemplate(
                                    text = contentValue.text,
                                    selectionStart = contentValue.selection.min,
                                    selectionEnd = contentValue.selection.max
                                )
                                contentValue = TextFieldValue(newText, TextRange(newCursor))
                                viewModel.updateContent(newText)
                            },
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }
                }
            }

            if (shouldShowBottomBar) {
                PostDetailBottomBar(
                    composerState = composerState,
                    inputValue = contentValue,
                    mediaPreviews = selectedMediaList.map {
                        ComposerMediaPreview(
                            uri = it.uri,
                            isVideo = it.type == PostCreateMediaType.Video
                        )
                    },
                    onValueChange = {
                        contentValue = it
                        viewModel.updateContent(it.text)
                    },
                    onComposerStateChange = { state ->
                        if (state == ComposerState.Media) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                        composerState = state
                        Log.d(TAG, "PostCreate composer state change: $state")
                    },
                    onSendClick = {},
                    onPickImageClick = {
                        Log.d(TAG, "PostCreate open image picker")
                        coroutineScope.launch {
                            val imageCount = selectedMediaList.count { it.type == PostCreateMediaType.Image }
                            val remaining = (MAX_POST_CREATE_IMAGES - imageCount).coerceAtLeast(0)
                            if (remaining == 0) {
                                ToastUtil.show(context, "最多添加 9 张图片")
                                return@launch
                            }
                            val picked = MediaPicker.pickImages(remaining)
                                .filter { it.type == MediaType.Image }
                            appendPickedMedia(
                                picked = picked,
                                selectedMediaList = selectedMediaList,
                                context = context
                            )
                            composerState = ComposerState.Media
                        }
                    },
                    onPickVideoClick = {
                        Log.d(TAG, "PostCreate open video picker")
                        coroutineScope.launch {
                            val picked = MediaPicker.pickVideo()
                            if (picked == null) {
                                ToastUtil.show(context, "暂不支持上传本地视频")
                                composerState = ComposerState.Media
                                return@launch
                            }
                            appendSelectedMedia(
                                uri = Uri.parse(picked.uri),
                                mediaType = PostCreateMediaType.Video,
                                selectedMediaList = selectedMediaList,
                                context = context
                            )
                            composerState = ComposerState.Media
                        }
                    },
                    onClearPreviewClick = {},
                    onRemovePreviewAt = { index ->
                        if (index in selectedMediaList.indices) {
                            val removed = selectedMediaList[index]
                            selectedMediaList.removeAt(index)
                            Log.d(
                                TAG,
                                "PostCreate remove media preview at index=$index, type=${removed.type}"
                            )
                        }
                    },
                    showReplyComposer = false
                )
            }
        }

        if (isPublishing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { },
                contentAlignment = Alignment.Center
            ) {
                AppLoadingIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary,
                    size = LoadingIndicatorSize.Large,
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun PostCreateViewPreview() {
    PostCreateView()
}

private fun appendPickedMedia(
    picked: List<PickedMedia>,
    selectedMediaList: SnapshotStateList<PostCreateSelectedMedia>,
    context: Context
) {
    if (picked.isEmpty()) {
        Log.d(TAG, "Image picker cancelled or returned no supported image")
        return
    }
    for (item in picked) {
        appendSelectedMedia(
            uri = Uri.parse(item.uri),
            mediaType = if (item.type == MediaType.Video) PostCreateMediaType.Video else PostCreateMediaType.Image,
            selectedMediaList = selectedMediaList,
            context = context
        )
    }
    Log.i(
        TAG,
        "Images appended: picked=${picked.size}, total=${selectedMediaList.size}"
    )
}

/**
 * 追加一条选中的媒体资源到创建页预览列表。
 * @param uri 系统选择器返回的媒体 Uri，null 代表取消选择
 * @param mediaType 媒体类型（图片或视频）
 * @param selectedMediaList 当前媒体列表状态
 */
private fun appendSelectedMedia(
    uri: Uri?,
    mediaType: PostCreateMediaType,
    selectedMediaList: SnapshotStateList<PostCreateSelectedMedia>,
    context: Context
) {
    if (uri == null) {
        Log.d(TAG, "Media picker cancelled, type=$mediaType")
        return
    }
    if (mediaType == PostCreateMediaType.Image) {
        val imageCount = selectedMediaList.count { it.type == PostCreateMediaType.Image }
        if (imageCount >= MAX_POST_CREATE_IMAGES) {
            ToastUtil.show(context, "最多添加 9 张图片")
            return
        }
        when (val vr = ImagePickValidator.validate(context, uri)) {
            is ImagePickValidator.Result.Ok -> {}
            else -> {
                ToastUtil.show(context, ImagePickValidator.toastMessage(vr))
                return
            }
        }
    }
    selectedMediaList.add(PostCreateSelectedMedia(uri = uri, type = mediaType, isRemote = false))
    Log.d(TAG, "Media selected: uri=$uri, type=$mediaType, total=${selectedMediaList.size}")
}

private const val TAG = "PostCreateView"
