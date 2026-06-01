package com.vortexa.ui.page.post.detail

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.vortexa.ui.component.LoadingButton
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.util.extension.click
import org.jetbrains.compose.resources.painterResource
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.default_pic
import vortexa.composeapp.generated.resources.icon_close
import vortexa.composeapp.generated.resources.icon_emoji
import vortexa.composeapp.generated.resources.icon_img

private const val TAG = "PostDetailBottomBar"
private val DEFAULT_EMOJI_PANEL_HEIGHT = 280.dp
private val MEDIA_PREVIEW_ITEM_SIZE = 72.dp
private val MEDIA_PREVIEW_CORNER_RADIUS = 8.dp

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
    showReplyComposer: Boolean = true,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val inputFocusRequester = remember { FocusRequester() }
    val density = LocalDensity.current
    val imeBottomPx = WindowInsets.ime.getBottom(density)
    val navigationBottomPx = WindowInsets.navigationBars.getBottom(density)
    var keyboardHeightPx by rememberSaveable { mutableIntStateOf(0) }
    var prevImePx by remember { mutableIntStateOf(0) }
    val isComposerExpanded = composerState != ComposerState.Collapsed
    val isKeyboardSlotReserved =
        composerState == ComposerState.Emoji || composerState == ComposerState.KeyboardPending
    val showEmojiPanelContent = composerState == ComposerState.Emoji
    val context = LocalContext.current
    val keyboardSlotHeight: Dp = if (keyboardHeightPx > 0) {
        with(density) { (keyboardHeightPx - navigationBottomPx).coerceAtLeast(0).toDp() }
            .takeIf { it > 0.dp }
            ?: DEFAULT_EMOJI_PANEL_HEIGHT
    } else {
        DEFAULT_EMOJI_PANEL_HEIGHT
    }

    val onEmojiToggleClick: () -> Unit = {
        val next = when (composerState) {
            ComposerState.Emoji -> ComposerState.KeyboardPending
            ComposerState.KeyboardPending -> ComposerState.Emoji
            else -> ComposerState.Emoji
        }
        onComposerStateChange(next)
        if (next == ComposerState.Emoji) {
            keyboardController?.hide()
        } else {
            keyboardController?.show()
        }
    }

    LaunchedEffect(imeBottomPx, composerState) {
        if (showReplyComposer && imeBottomPx > 0) {
            when (composerState) {
                ComposerState.Keyboard -> {
                    if (keyboardHeightPx == 0 || imeBottomPx >= prevImePx) {
                        keyboardHeightPx = imeBottomPx
                    }
                }
                ComposerState.KeyboardPending -> Unit
                ComposerState.Collapsed,
                ComposerState.Media -> {
                    if (prevImePx == 0) {
                        onComposerStateChange(ComposerState.Keyboard)
                    }
                }
                ComposerState.Emoji -> Unit
            }
        }
        prevImePx = imeBottomPx
    }

    LaunchedEffect(composerState) {
        when {
            showReplyComposer && composerState == ComposerState.Keyboard -> {
                withFrameNanos { }
                inputFocusRequester.requestFocus()
                keyboardController?.show()
            }
            showReplyComposer && composerState == ComposerState.KeyboardPending -> {
                withFrameNanos { }
                inputFocusRequester.requestFocus()
                keyboardController?.show()
            }
            showReplyComposer && composerState == ComposerState.Emoji -> {
                keyboardController?.hide()
            }
            showReplyComposer && composerState == ComposerState.Collapsed -> {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
            showReplyComposer && composerState == ComposerState.Media -> {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
            !showReplyComposer && composerState == ComposerState.Media -> {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        }
    }

    Column(
        modifier
            .fillMaxWidth()
            .background(Color.White)
            .then(
                if (composerState == ComposerState.Keyboard) {
                    Modifier.imePadding()
                } else {
                    Modifier.navigationBarsPadding()
                }
            )
            .then(
                when {
                    !showReplyComposer -> Modifier.heightIn(min = 60.dp)
                    isComposerExpanded -> Modifier.heightIn(min = 102.dp)
                    else -> Modifier.height(48.dp)
                }
            )
            .padding(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 8.dp)
            .then(
                if (showReplyComposer && composerState == ComposerState.Collapsed) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onComposerStateChange(ComposerState.Keyboard) }
                } else {
                    Modifier
                }
            ),
    ) {
        val showMediaPreviewStrip =
            mediaPreviews.isNotEmpty() && (!showReplyComposer || isComposerExpanded)
        AnimatedVisibility(visible = showMediaPreviewStrip) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                mediaPreviews.forEachIndexed { index, item ->
                    MediaPreviewItem(
                        uri = item.uri,
                        isVideo = item.isVideo,
                        context = context,
                        onRemove = { onRemovePreviewAt(index) },
                    )
                }
            }
        }

        if (showReplyComposer) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .then(
                            if (isComposerExpanded) {
                                Modifier.fillMaxWidth()
                            } else {
                                Modifier.weight(1f)
                            }
                        )
                        .heightIn(min = 40.dp, max = 72.dp)
                        .background(Colors.gray_F3F5F7, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.TopStart,
                ) {
                    if (isComposerExpanded) {
                        BasicTextField(
                            value = inputValue,
                            onValueChange = onValueChange,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .focusRequester(inputFocusRequester)
                                .fillMaxWidth(),
                            textStyle = TextStyle(
                                color = Colors.black_101828,
                                fontSize = 16.sp,
                            ),
                            maxLines = 3,
                            cursorBrush = SolidColor(Colors.blue_3266FF),
                            decorationBox = { innerTextField ->
                                if (inputValue.text.isEmpty()) {
                                    Text(
                                        text = "说点什么...",
                                        color = Colors.gray_B1B8C6,
                                        fontSize = 16.sp,
                                    )
                                }
                                innerTextField()
                            },
                        )
                    } else {
                        Text(
                            text = if (inputValue.text.isEmpty()) "说点什么..." else inputValue.text,
                            color = Colors.gray_B1B8C6,
                            fontSize = 14.sp,
                            maxLines = 1,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .click { onComposerStateChange(ComposerState.Keyboard) },
                        )
                    }
                }

                if (!isComposerExpanded) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Image(
                            painter = painterResource(Res.drawable.icon_emoji),
                            contentDescription = "Emoji",
                            modifier = Modifier
                                .size(24.dp)
                                .click { onEmojiToggleClick() },
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(64.dp, 32.dp)
                                .background(
                                    color = Colors.black_101828,
                                    shape = RoundedCornerShape(16.dp),
                                ),
                        ) {
                            Text(
                                text = "回复",
                                style = FontMedium(14, Color.White),
                                modifier = Modifier.align(Alignment.Center),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            modifier = Modifier
                .padding(top = if (showReplyComposer) 8.dp else 0.dp)
                .height(44.dp),
            visible = !showReplyComposer || isComposerExpanded,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
        ) {
            Row(
                Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(Res.drawable.icon_emoji),
                    contentDescription = "Emoji",
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .padding(vertical = 10.dp)
                        .size(24.dp)
                        .click { onEmojiToggleClick() },
                )
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
                        },
                )
                Spacer(modifier = Modifier.weight(1f))

                if (showReplyComposer) {
                    LoadingButton(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(54.dp, 24.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Colors.black_101828),
                        text = "回复",
                        isLoading = replyLoading,
                        onClick = onSendClick,
                        content = {
                            Text(
                                text = "回复",
                                style = FontMedium(13, Color.White),
                                maxLines = 1,
                            )
                        },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isKeyboardSlotReserved,
            enter = fadeIn(),
            exit = ExitTransition.None,
        ) {
            if (showEmojiPanelContent) {
                PostDetailEmojiPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keyboardSlotHeight),
                    onEmojiClick = { emoji ->
                        val text = inputValue.text
                        val sel = inputValue.selection
                        val insertStart = sel.min
                        val insertEnd = sel.max
                        val newText = text.take(insertStart) + emoji + text.drop(insertEnd)
                        val cursorAfter = insertStart + emoji.length
                        onValueChange(TextFieldValue(newText, TextRange(cursorAfter)))
                    },
                )
            } else {
                Spacer(modifier = Modifier.height(keyboardSlotHeight))
            }
        }
    }
}

@Composable
private fun MediaPreviewItem(
    uri: Uri,
    isVideo: Boolean,
    context: Context,
    onRemove: () -> Unit,
) {
    val previewShape = RoundedCornerShape(MEDIA_PREVIEW_CORNER_RADIUS)
    val placeholder = painterResource(Res.drawable.default_pic)
    Box(modifier = Modifier.size(MEDIA_PREVIEW_ITEM_SIZE)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(previewShape)
                .background(Colors.gray_F3F5F7),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = uri.toString(),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                placeholder = placeholder,
                error = placeholder,
                fallback = placeholder,
            )
            if (isVideo) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.28f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "VIDEO",
                        color = Color.White,
                        fontSize = 12.sp,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(18.dp)
                .click(onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(Res.drawable.icon_close), contentDescription = "")
        }
    }
}
