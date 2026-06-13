package com.vortexa.ui.page.teach.video

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import com.vortexa.ui.component.AppLoadingIndicator
import com.vortexa.ui.component.LoadingIndicatorSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vortexa.platform.rtc.RtcVideoSurface
import com.vortexa.platform.rtc.rememberRtcEngineController
import com.vortexa.ui.component.AvatarImage
import com.vortexa.ui.theme.BaseTheme
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontSemiBold
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.util.ToastUtil
import org.jetbrains.compose.resources.painterResource
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.icon_microphone
import vortexa.composeapp.generated.resources.icon_microphone_off
import vortexa.composeapp.generated.resources.icon_mute
import vortexa.composeapp.generated.resources.icon_phone
import vortexa.composeapp.generated.resources.icon_record
import vortexa.composeapp.generated.resources.icon_share_screen
import vortexa.composeapp.generated.resources.icon_share_screen_green
import vortexa.composeapp.generated.resources.icon_unmute
import vortexa.composeapp.generated.resources.icon_watch
import kotlinx.coroutines.delay

data class VideoRtcBottomBarState(
    val micMuted: Boolean = true,
    val speakerMuted: Boolean = false,
)

data class VideoRtcUser(
    val agoraUid: Int,
    val name: String,
    val isTutor: Boolean,
    val avatarUrl: String? = null,
)

@Composable
fun VideoRtcRoute(
    channelName: String,
    courseTeacherId: Long,
    courseStartTimeMs: Long? = null,
    courseEndTimeMs: Long? = null,
    onClose: () -> Unit,
) {
    BaseTheme(belowStatusBar = true, aboveNavigationBar = true) {
        VideoRtcView(
            channelName = channelName,
            courseTeacherId = courseTeacherId,
            courseStartTimeMs = courseStartTimeMs,
            courseEndTimeMs = courseEndTimeMs,
            onClose = onClose,
        )
    }
}

@Composable
fun VideoRtcView(
    channelName: String,
    courseTeacherId: Long,
    courseStartTimeMs: Long? = null,
    courseEndTimeMs: Long? = null,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: VideoRtcViewModel = vortexaViewModel(
        key = "video-rtc-$channelName-$courseTeacherId-$courseStartTimeMs-$courseEndTimeMs"
    ) {
        VideoRtcViewModel(channelName, courseStartTimeMs, courseEndTimeMs, courseTeacherId)
    },
) {
    val controller = rememberRtcEngineController()
    val elapsedTimeText by viewModel.elapsedTimeText.collectAsState()
    val token by viewModel.token.collectAsState()
    val tokenLoading by viewModel.tokenLoading.collectAsState()
    val tokenError by viewModel.tokenError.collectAsState()
    val agoraUid by viewModel.agoraUid.collectAsState()
    val targetUid by viewModel.targetUid.collectAsState()
    val bottomBarState by viewModel.bottomBarState.collectAsState()
    val joinedUsers by viewModel.joinedUsers.collectAsState()
    val screenSharing by viewModel.screenSharing.collectAsState()
    val lastTenMinutesReminder by viewModel.lastTenMinutesReminder.collectAsState()
    val courseTimeEnded by viewModel.courseTimeEnded.collectAsState()
    val joinedUserCount = joinedUsers.size

    var idleResetKey by remember { mutableIntStateOf(0) }
    var userListVisible by remember { mutableStateOf(true) }

    LaunchedEffect(controller) {
        viewModel.attachRtcController(controller)
    }
    LaunchedEffect(token, controller) {
        if (token.isNotBlank()) viewModel.joinRtc(controller)
    }
    LaunchedEffect(courseTimeEnded) {
        if (courseTimeEnded) {
            ToastUtil.show("课程已结束")
            viewModel.leaveRtc()
            onClose()
        }
    }
    LaunchedEffect(idleResetKey, joinedUserCount) {
        userListVisible = true
        delay(3_000)
        userListVisible = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Colors.black_101828)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    idleResetKey++
                })
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            VideoRtcTopBar(durationText = elapsedTimeText)
            AnimatedVisibility(
                visible = userListVisible,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)),
            ) {
                VideoRtcUserList(
                    users = joinedUsers,
                    selectedUid = targetUid,
                    onUserClick = viewModel::switchTargetUid,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    tokenLoading -> AppLoadingIndicator(
                        color = Color.White,
                        size = LoadingIndicatorSize.Large,
                    )
                    tokenError != null -> VideoRtcErrorContent(message = tokenError.orEmpty())
                    token.isNotBlank() -> RtcVideoSurface(
                        controller = controller,
                        localUid = agoraUid,
                        targetUid = targetUid,
                        screenSharing = screenSharing,
                        modifier = Modifier.fillMaxSize(),
                    )
                    else -> VideoRtcErrorContent(message = "课堂连接未就绪")
                }
            }
            VideoRtcBottomBar(
                state = bottomBarState,
                screenSharing = screenSharing,
                onMicClick = { viewModel.setMicMuted(!bottomBarState.micMuted) },
                onSpeakerClick = viewModel::toggleSpeakerMuted,
                onHangupClick = {
                    viewModel.leaveRtc()
                    onClose()
                },
                onScreenShareClick = { ToastUtil.show("屏幕共享暂未接入") },
            )
        }

        if (lastTenMinutesReminder) {
            AlertDialog(
                onDismissRequest = viewModel::dismissLastTenMinutesReminder,
                title = { Text("提示") },
                text = { Text("课程剩余最后10分钟") },
                confirmButton = {
                    TextButton(onClick = viewModel::dismissLastTenMinutesReminder) {
                        Text("知道了")
                    }
                },
            )
        }
    }
}

@Composable
private fun VideoRtcTopBar(
    durationText: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = durationText,
            style = FontMedium(fontSize = 16, color = Color.White),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            painter = painterResource(Res.drawable.icon_watch),
            contentDescription = "计时",
            tint = Color.White,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun VideoRtcUserList(
    users: List<VideoRtcUser>,
    selectedUid: Int,
    onUserClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        users.forEach { user ->
            VideoRtcUserItem(
                user = user,
                selected = user.agoraUid == selectedUid,
                onClick = { onUserClick(user.agoraUid) },
            )
        }
    }
}

@Composable
private fun VideoRtcUserItem(
    user: VideoRtcUser,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = when {
        selected -> Color.White
        user.isTutor -> Colors.blue_74B9FD
        else -> Color.White.copy(alpha = 0.2f)
    }
    val tagBg = if (user.isTutor) Colors.blue_277DFF else Colors.black_101828

    Column(
        modifier = modifier
            .width(84.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .border(width = if (selected) 2.dp else 1.dp, color = borderColor, shape = CircleShape),
            contentAlignment = Alignment.BottomCenter,
        ) {
            AvatarImage(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Colors.black_101828),
                avatarUrl = user.avatarUrl,
                contentDescription = user.name,
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(tagBg, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(horizontal = 10.dp, vertical = 2.dp),
            ) {
                Text(
                    text = if (user.isTutor) "导师" else "学员",
                    style = FontMedium(fontSize = 12, color = Color.White),
                )
            }
        }
        Text(
            text = user.name,
            style = FontSemiBold(fontSize = 15, color = Color.White),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun VideoRtcErrorContent(
    message: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = message,
        color = Color.White,
        modifier = modifier.padding(horizontal = 24.dp),
    )
}

@Composable
private fun VideoRtcBottomBar(
    state: VideoRtcBottomBarState,
    screenSharing: Boolean,
    onMicClick: () -> Unit,
    onSpeakerClick: () -> Unit,
    onHangupClick: () -> Unit,
    onScreenShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 18.dp, end = 18.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconImageButton(
            onClick = onMicClick,
            contentDescription = if (state.micMuted) "取消静音" else "静音",
            icon = if (state.micMuted) Res.drawable.icon_microphone_off else Res.drawable.icon_microphone,
        )
        IconImageButton(
            onClick = onSpeakerClick,
            contentDescription = if (state.speakerMuted) "取消扬声器静音" else "扬声器静音",
            icon = if (state.speakerMuted) Res.drawable.icon_mute else Res.drawable.icon_unmute,
        )
        IconButton(
            onClick = onHangupClick,
            modifier = Modifier
                .width(80.dp)
                .padding(horizontal = 6.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(Color(0xFFFF3B2F), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.icon_phone),
                    contentDescription = "挂断",
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        IconImageButton(
            onClick = onScreenShareClick,
            contentDescription = "屏幕共享",
            icon = if (screenSharing) Res.drawable.icon_share_screen_green else Res.drawable.icon_share_screen,
        )
        IconImageButton(
            onClick = { ToastUtil.show("该功能暂未开放，敬请期待") },
            contentDescription = "录制",
            icon = Res.drawable.icon_record,
        )
    }
}

@Composable
private fun IconImageButton(
    onClick: () -> Unit,
    contentDescription: String,
    icon: org.jetbrains.compose.resources.DrawableResource,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(54.dp),
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = contentDescription,
        )
    }
}
