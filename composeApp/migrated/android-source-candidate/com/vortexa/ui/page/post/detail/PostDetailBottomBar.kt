package com.vortexa.ui.page.post.detail

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.util.Size
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import com.vortexa.ui.component.LoadingButton
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.util.extension.click
import com.vortexa.util.pxToDp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vortexa.composeapp.generated.resources.Res

/**
 * 底栏媒体预览项：图片与视频共用同一套尺寸、圆角与横向排列规则。
 * @param uri 媒体内容 Uri
 * @param isVideo 是否为视频（缩略图上叠加播放标识）
 */
data class ComposerMediaPreview(
    val uri: Uri,
    val isVideo: Boolean
)

/**
 * 帖子详情页底部回复栏，由 [ComposerState] 状态机驱动键盘/表情/媒体面板。
 *
 * @param modifier 外层修饰符
 * @param composerState 当前输入区状态（Collapsed/Keyboard/Emoji/Media）
 * @param inputValue 输入框内容与选区
 * @param mediaPreviews 已选媒体预览列表（图片/视频同一行展示）
 * @param onValueChange 内容/选区变化回调
 * @param onComposerStateChange 状态切换回调
 * @param onSendClick 点击发送回调
 * @param replyLoading 发送请求加载态，true 时发送按钮显示转圈并禁用
 * @param onPickImageClick 点击图片图标回调
 * @param onPickVideoClick 点击视频图标回调
 * @param onClearPreviewClick 清空全部预览回调
 * @param onRemovePreviewAt 移除指定索引的预览回调
 * @param showReplyComposer 为 false 时仅展示工具条（图/视频/表情等），隐藏输入框、输入旁展开按钮与「回复」按钮（发帖页等场景）
 */
@Composable
fun PostDetailBottomBar(
    modifier: Modifier = Modifier,
    composerState: ComposerState = ComposerState.Collapsed,
    inputValue: TextFieldValue = TextFieldValue(""),
    mediaPreviews: List<ComposerMediaPreview> = emptyList(),
    onValueChange: (TextFieldValue) -> Unit = {},
    onComposerStateChange: (ComposerState) -> Unit = {},
    onSendClick: () -> Unit = {},
    replyLoading: Boolean = false,
    onPickImageClick: () -> Unit = {},
    onPickVideoClick: () -> Unit = {},
    onClearPreviewClick: () -> Unit = {},
    onRemovePreviewAt: (Int) -> Unit = {},
    showReplyComposer: Boolean = true
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val density = LocalDensity.current
    val imeBottomPx = WindowInsets.ime.getBottom(density)
    var keyboardHeightPx by rememberSaveable { mutableIntStateOf(0) }
    var prevImePx by remember { mutableIntStateOf(0) }
    val isComposerExpanded = composerState != ComposerState.Collapsed
    val isEmojiPanelVisible = composerState == ComposerState.Emoji

    /** 表情按钮：切到 Emoji 或从 Emoji 切回 Keyboard */
    val onEmojiToggleClick: () -> Unit = {
        val next =
            if (composerState == ComposerState.Emoji) ComposerState.Keyboard else ComposerState.Emoji
        Log.d(TAG, "Emoji toggle: $composerState -> $next")
        onComposerStateChange(next)
        if (next == ComposerState.Emoji) {
            keyboardController?.hide()
        } else if (showReplyComposer) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            // 工具条模式：正文输入在页面其它处，仅尝试唤起系统键盘
            keyboardController?.show()
        }
    }
    val emojiPanelHeight: Dp = if (keyboardHeightPx > 0) {
        with(density) { keyboardHeightPx.toDp() }
    } else {
        DEFAULT_EMOJI_PANEL_HEIGHT
    }

    // 仅当键盘从无到有时切到 Keyboard，避免收起过程中高度渐变把状态误置为 Keyboard
    LaunchedEffect(imeBottomPx) {
        if (showReplyComposer && imeBottomPx > 0 && prevImePx == 0) {
            Log.d(TAG, "Keyboard appeared (ime 0->$imeBottomPx), transition to Keyboard state")
            onComposerStateChange(ComposerState.Keyboard)
        }
        prevImePx = imeBottomPx
    }

    // 仅 Keyboard 态且 ime>0 时缓存高度，供表情面板用；不收听高度驱动状态
    LaunchedEffect(imeBottomPx, composerState) {
        if (!showReplyComposer) return@LaunchedEffect
        if (imeBottomPx > 0 && composerState == ComposerState.Keyboard) {
            keyboardHeightPx = imeBottomPx
            Log.d(TAG, "Cache keyboard height for emoji panel, px=$keyboardHeightPx")
        }
    }

    LaunchedEffect(composerState) {
        when {
            showReplyComposer && composerState == ComposerState.Keyboard -> {
                focusRequester.requestFocus()
                keyboardController?.show()
                Log.d(TAG, "State machine: show keyboard")
            }

            showReplyComposer -> {
                keyboardController?.hide()
                Log.d(TAG, "State machine: hide keyboard")
            }

            composerState == ComposerState.Keyboard -> {
                // 工具条模式：正文输入不在底栏，不抢焦点、不收起主输入框键盘
                Log.d(TAG, "State machine: toolbar-only, skip keyboard control for Keyboard state")
            }

            composerState == ComposerState.Collapsed -> {
                // 工具条模式：Collapsed 是常态；底栏在「标题↔正文」切换时会整段重组，
                // 若此处 hide，会在正文 BasicTextField 刚获得焦点时立刻收起键盘。
                Log.d(TAG, "State machine: toolbar-only, Collapsed — skip hide")
            }

            else -> {
                keyboardController?.hide()
                Log.d(TAG, "State machine: toolbar-only, hide keyboard for panel state")
            }
        }
    }
    val context = LocalContext.current
    val screenWidth =
        pxToDp(LocalContext.current.resources.displayMetrics.widthPixels.toFloat(), context).dp
    val animatedWidth by animateDpAsState(
        targetValue = if (!isComposerExpanded) {
            (screenWidth - 184.dp - 18.dp)
        } else {
            (screenWidth - 28.dp)
        },
        label = "animatedWidth",
    )
    Column(
        modifier
            .fillMaxWidth()
            .background(Color.White)
            .then(
                when {
                    !showReplyComposer -> Modifier.heightIn(min = 60.dp)
                    isComposerExpanded -> Modifier.heightIn(min = 102.dp)
                    else -> Modifier.height(48.dp)
                }
            )
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .then(
                if (showReplyComposer && composerState == ComposerState.Collapsed) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onComposerStateChange(ComposerState.Keyboard) }
                } else {
                    Modifier
                }
            ),
    ) {
        // 发帖工具条模式：有图即显示，避免键盘收起回到 Collapsed 时预览被误藏；详情回复区仅在展开时显示
        val showMediaPreviewStrip =
            mediaPreviews.isNotEmpty() && (!showReplyComposer || isComposerExpanded)
        AnimatedVisibility(visible = showMediaPreviewStrip) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                mediaPreviews.forEachIndexed { index, item ->
                    MediaPreviewItem(
                        uri = item.uri,
                        isVideo = item.isVideo,
                        context = context,
                        onRemove = { onRemovePreviewAt(index) }
                    )
                }
            }
        }

        if (showReplyComposer) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 输入框区域：最小 40.dp，最多 3 行高度（约 14.sp * 1.2 * 3 ≈ 60.dp 内容 + 上下边距）
                Box(
                    modifier = Modifier
                        .width(animatedWidth)
                        .heightIn(min = 40.dp, max = 72.dp)
                        .background(Colors.gray_F3F5F7, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (isComposerExpanded) {
                        BasicTextField(
                            value = inputValue,
                            onValueChange = onValueChange,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                color = Colors.black_101828,
                                fontSize = 16.sp
                            ),
                            maxLines = 3,
                            cursorBrush = SolidColor(Colors.blue_3266FF),
                            decorationBox = { innerTextField ->
                                if (inputValue.text.isEmpty()) {
                                    Text(
                                        text = "说点什么...",
                                        color = Colors.gray_B1B8C6,
                                        fontSize = 16.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                    } else {
                        Text(
                            text = if (inputValue.text.isEmpty()) "说点什么..." else inputValue.text,
                            color = Colors.gray_B1B8C6,
                            fontSize = 14.sp,
                            maxLines = 1,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .click { onComposerStateChange(ComposerState.Keyboard) }
                        )
                    }
                }
//                AnimatedVisibility(
//                    modifier = Modifier,
//                    visible = isComposerExpanded,
//                    enter = slideInHorizontally { it } + fadeIn(),
//                    exit = slideOutHorizontally { it } + fadeOut()
//                ) {
//
//                    // 表情图标
//                    Image(
//                        painter = painterResource(Res.drawable.icon_expend),
//                        contentDescription = "Emoji",
//                        modifier = Modifier
//                            .padding(start = 10.dp)
//                            .size(25.dp)
//                            .click { onEmojiToggleClick() }
//                    )
//                }

                if (!isComposerExpanded) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(12.dp))
                        // 表情图标
                        Image(
                            painter = painterResource(Res.drawable.icon_emoji),
                            contentDescription = "Emoji",
                            modifier = Modifier
                                .size(24.dp)
                                .click { onEmojiToggleClick() }
                        )
                        Spacer(modifier = Modifier.width(36.dp))
                        // 回复按钮
                        Box(
                            modifier = Modifier
                                .size(90.dp, 32.dp)
                                .background(
                                    color = Colors.black_101828,
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Text(
                                stringResource(R.string.reply),
                                style = FontMedium(14, Color.White),
                                modifier = Modifier.align(Alignment.Center),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 额外的图标（图片、@等），仅在输入激活时显示；发帖工具条模式始终显示
        AnimatedVisibility(
            modifier = Modifier
                .padding(top = if (showReplyComposer) 8.dp else 0.dp)
                .height(44.dp),
            visible = !showReplyComposer || isComposerExpanded,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut()
        ) {
            Row(
                Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 表情图标
                Image(
                    painter = painterResource(Res.drawable.icon_emoji),
                    contentDescription = "Emoji",
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .padding(vertical = 10.dp)
                        .size(24.dp)
                        .click { onEmojiToggleClick() }
                )

                // 示例图标：图片
                Image(
                    painter = painterResource(Res.drawable.icon_img),
                    contentDescription = "Image",
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 10.dp)
                        .size(24.dp)
                        .click {
                            onComposerStateChange(ComposerState.Media)
                            onPickImageClick()
                        }
                )
//                Image(
//                    painter = painterResource(Res.drawable.icon_video),
//                    contentDescription = "At",
//                    modifier = Modifier
//                        .padding(horizontal = 16.dp)
//                        .padding(vertical = 10.dp)
//                        .size(24.dp)
//                        .click {
//                            onComposerStateChange(ComposerState.Media)
//                            onPickVideoClick()
//                        }
//                )
//                // 示例图标：图片
//                Image(
//                    painter = painterResource(Res.drawable.icon_link),
//                    contentDescription = "Image",
//                    modifier = Modifier
//                        .padding(horizontal = 16.dp)
//                        .padding(vertical = 10.dp)
//                        .size(24.dp)
//                        .click { }
//                )
                Spacer(modifier = Modifier.weight(1f))

                if (showReplyComposer) {
                    // 回复按钮：LoadingButton，发送请求中显示转圈并禁用
                    LoadingButton(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(54.dp, 24.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Colors.black_101828),
                        text = stringResource(R.string.reply),
                        isLoading = replyLoading,
                        onClick = onSendClick,
                        content = {
                            Text(
                                stringResource(R.string.reply),
                                style = FontMedium(13, Color.White),
                                maxLines = 1
                            )
                        }
                    )
                }
            }
        }

        key(composerState) {
            AnimatedVisibility(
                visible = isEmojiPanelVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                PostDetailEmojiPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(emojiPanelHeight),
                    onEmojiClick = { emoji ->
                        val text = inputValue.text
                        val sel = inputValue.selection
                        val insertStart = sel.min
                        val insertEnd = sel.max
                        val newText = text.take(insertStart) + emoji + text.drop(insertEnd)
                        val cursorAfter = insertStart + emoji.length
                        onValueChange(TextFieldValue(newText, TextRange(cursorAfter)))
                        Log.d(TAG, "Insert emoji at $insertStart, cursor at $cursorAfter")
                    }
                )
            }
        }
    }
}

@Composable
@Preview
fun PostDetailBottomBarPreView() {
    PostDetailBottomBar(composerState = ComposerState.Emoji)
}

/**
 * 单张媒体预览项：固定正方形圆角容器，缩略图 CenterCrop；视频叠加半透明遮罩与播放图标（图标来自 Material Icons，Apache 2.0）。
 * @param uri 媒体 Uri
 * @param isVideo 是否为视频
 * @param context 用于加载缩略图
 * @param onRemove 点击删除回调
 */
@Composable
private fun MediaPreviewItem(
    uri: Uri,
    isVideo: Boolean,
    context: Context,
    onRemove: () -> Unit
) {
    val density = LocalDensity.current
    val thumbPx = with(density) { MEDIA_PREVIEW_ITEM_SIZE.roundToPx().coerceAtLeast(1) }
    val bitmap by produceState<ImageBitmap?>(initialValue = null, uri, thumbPx, isVideo) {
        value = withContext(Dispatchers.IO) {
            loadComposerMediaThumbnail(
                context = context,
                uri = uri,
                isVideo = isVideo,
                thumbPx = thumbPx
            )
        }
    }
    val previewShape = RoundedCornerShape(MEDIA_PREVIEW_CORNER_RADIUS)
    Box(modifier = Modifier.size(MEDIA_PREVIEW_ITEM_SIZE)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(previewShape)
                .background(Colors.gray_F3F5F7),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!,
                    contentDescription = "Media preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                Text(text = "…", color = Colors.gray_B1B8C6, fontSize = 12.sp)
            }
            if (isVideo) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = stringResource(R.string.media_preview_play),
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(18.dp)
                .click(onRemove),
            contentAlignment = Alignment.Center
        ) {
            Image(painterResource(Res.drawable.icon_close), contentDescription = "")
        }
    }
}

private const val TAG = "PostDetailBottomBar"
private val DEFAULT_EMOJI_PANEL_HEIGHT = 280.dp

/** 底栏媒体预览缩略图边长（正方形），与输入条/工具条垂直紧贴排列 */
private val MEDIA_PREVIEW_ITEM_SIZE = 72.dp

private val MEDIA_PREVIEW_CORNER_RADIUS = 8.dp

/**
 * 加载底栏预览图：图片用 [ContentResolver.loadThumbnail]；视频在该 API 对 Photo Picker 等 Uri 常返回失败，失败则用 [MediaMetadataRetriever] 抽帧。
 * @param context 上下文
 * @param uri 媒体 Uri
 * @param isVideo 是否为视频
 * @param thumbPx 缩略图最大边像素（用于 loadThumbnail 与降采样上限）
 * @return Compose 位图，失败返回 null
 */
private fun loadComposerMediaThumbnail(
    context: Context,
    uri: Uri,
    isVideo: Boolean,
    thumbPx: Int
): ImageBitmap? {
    if (!isVideo) {
        return runCatching {
            context.contentResolver
                .loadThumbnail(uri, Size(thumbPx, thumbPx), null)
                .asImageBitmap()
        }.onFailure { Log.w(TAG, "Image loadThumbnail failed, uri=$uri", it) }.getOrNull()
    }
    runCatching {
        context.contentResolver
            .loadThumbnail(uri, Size(thumbPx, thumbPx), null)
            .asImageBitmap()
    }.getOrNull()?.let {
        Log.d(TAG, "Video cover via loadThumbnail, uri=$uri")
        return it
    }
    val frame = extractVideoFrameBitmap(context, uri, thumbPx)
    if (frame == null) {
        Log.w(TAG, "Video cover unavailable (no frame), uri=$uri")
        return null
    }
    Log.d(TAG, "Video cover via MediaMetadataRetriever, uri=$uri")
    return frame.asImageBitmap()
}

/**
 * 用 MediaMetadataRetriever 从视频中取一帧并限制最大边长，避免 OOM。
 * @param context 上下文
 * @param uri 视频 Uri
 * @param maxEdgePx 长边上限像素
 * @return 位图；调用方负责展示生命周期内勿 recycle（交给 [ImageBitmap] 使用）
 */
private fun extractVideoFrameBitmap(context: Context, uri: Uri, maxEdgePx: Int): Bitmap? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        val frame =
            retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.getFrameAtTime(
                    1_000_000L,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
        if (frame == null) {
            Log.w(TAG, "getFrameAtTime returned null, uri=$uri")
            null
        } else {
            downscaleBitmapMaxEdge(frame, maxEdgePx)
        }
    } catch (e: Throwable) {
        Log.w(TAG, "MediaMetadataRetriever failed, uri=$uri", e)
        null
    } finally {
        runCatching { retriever.release() }
    }
}

/**
 * 将位图长边缩小到不超过 [maxEdgePx]，避免大图占内存。
 * @param src 源图
 * @param maxEdgePx 长边上界
 * @return 缩放后的图；若新建了实例会 recycle 原图
 */
private fun downscaleBitmapMaxEdge(src: Bitmap, maxEdgePx: Int): Bitmap? {
    val w = src.width
    val h = src.height
    if (w <= 0 || h <= 0) {
        src.recycle()
        return null
    }
    val maxDim = maxOf(w, h)
    if (maxDim <= maxEdgePx) return src
    val scale = maxEdgePx.toFloat() / maxDim
    val nw = (w * scale).toInt().coerceAtLeast(1)
    val nh = (h * scale).toInt().coerceAtLeast(1)
    val scaled = Bitmap.createScaledBitmap(src, nw, nh, true)
    if (scaled != src) src.recycle()
    return scaled
}
