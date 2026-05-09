package com.vortexa.ui.page.teach.video

import android.app.Activity
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vortexa.ui.component.RtcPlayView
import com.vortexa.ui.theme.Colors
import com.vortexa.util.ToastUtil
import com.vortexa.util.findActivity

/**
 * 1对1 视频直播页：顶部栏 + 用户列表 + 视频容器(weight=1) + 底部按钮组。
 * 需传入 [channelName]，请求声网 Token 后延迟初始化 RtcEngine；Token 获取前展示 Loading。
 *
 * @param channelName 声网频道名，必填
 * @param courseTeacherId 本节课导师 ID，与用户资料中的 [com.vortexa.model.UserProfileInfo.teacherId] 比对以展示导师/学员标签
 * @param courseEndTimeMs 课程结束时间（epoch 毫秒），来自订单详情；null 则不下课提醒、不强制关页
 */
@Composable
fun VideoRtcView(
    channelName: String,
    modifier: Modifier = Modifier,
    courseTeacherId: Long,
    /** 课程开始时间（epoch 毫秒），来自订单详情；null 则从进入页时刻起显示正计时 */
    courseStartTimeMs: Long? = null,
    courseEndTimeMs: Long? = null
) {
    val viewModel = viewModel(
        modelClass = VideoViewModel::class.java,
        factory = VideoViewModelFactory(channelName, courseStartTimeMs, courseEndTimeMs, courseTeacherId)
    )
    val elapsedTimeText by viewModel.elapsedTimeText.collectAsState()
    val channel by viewModel.channelName.collectAsState()
    val token by viewModel.token.collectAsState()
    val tokenLoading by viewModel.tokenLoading.collectAsState()
    val tokenError by viewModel.tokenError.collectAsState()
    val agoraUid by viewModel.agoraUid.collectAsState()
    val targetUid by viewModel.targetUid.collectAsState()
    val context = LocalContext.current
    val rtcEngine by viewModel.rtcEngine.collectAsState()
    val bottomBarState by viewModel.bottomBarState.collectAsState()
    val joinedUsers by viewModel.joinedUsers.collectAsState()
    val screenSharing by viewModel.screenSharingState.collectAsState()
    val lastTenMinutesReminder by viewModel.lastTenMinutesReminder.collectAsState()
    val courseTimeEnded by viewModel.courseTimeEnded.collectAsState()
    val joinedUserCount = joinedUsers.size

    var idleResetKey by remember { mutableIntStateOf(0) }
    var userListVisible by remember { mutableStateOf(true) }
    val listAnimMs = 300

    LaunchedEffect(idleResetKey, joinedUserCount) {
        userListVisible = true
        delay(3_000)
        userListVisible = false
    }

    val recordAudioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setMicMuted(false)
        } else {
            ToastUtil.show("需要麦克风权限才能开启麦克风")
        }
    }

    // Token 获取成功后初始化 RtcEngine
    LaunchedEffect(token) {
        if (token.isNotBlank()) {
            viewModel.generateRtcEngine(context)
        }
    }

    LaunchedEffect(courseTimeEnded) {
        if (!courseTimeEnded) return@LaunchedEffect
        val act = context.findActivity()
        act.setResult(
            Activity.RESULT_OK,
            Intent().putExtra(VideoRtcActivity.EXTRA_RESULT_REFRESH_PREVIOUS, true)
        )
        act.finish()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Colors.black_101828)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.changes.any { it.pressed && !it.previousPressed }) {
                            idleResetKey++
                        }
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            VideoRtcTopBar(
                durationText = elapsedTimeText,
                onMenuClick = { }
            )
            AnimatedVisibility(
                visible = userListVisible,
                enter = expandVertically(animationSpec = tween(listAnimMs)) +
                    fadeIn(animationSpec = tween(listAnimMs)),
                exit = shrinkVertically(animationSpec = tween(listAnimMs)) +
                    fadeOut(animationSpec = tween(listAnimMs))
            ) {
                VideoRtcUserList(users = joinedUsers)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                   
            ) {
                when {
                    tokenLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                    tokenError != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            VideoRtcErrorContent(message = tokenError!!)
                        }
                    }
                    rtcEngine != null && token.isNotBlank() -> {
                        RtcPlayView(
                            rtcEngine = rtcEngine!!,
                            token = token,
                            channelName = channel,
                            agoraUid = agoraUid,
                            targetUid = targetUid,
                            screenSharing = screenSharing,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            VideoRtcBottomBar(
                state = bottomBarState,
                screenSharing = screenSharing,
                onMicClick = {
                    if (bottomBarState.micMuted) {
                        when {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                                PackageManager.PERMISSION_GRANTED -> viewModel.setMicMuted(false)
                            else -> recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    } else {
                        viewModel.setMicMuted(true)
                    }
                },
                onSpeakerClick = { viewModel.onSpeakerMuteClick(context) },
                onHangupClick = { viewModel.onHangupClick(context) },
                onScreenShareClick = { viewModel.requestScreenSharing(context) },
            )
        }
        if (lastTenMinutesReminder) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissLastTenMinutesReminder() },
                title = { Text("提示") },
                text = { Text("课程剩余最后10分钟") },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissLastTenMinutesReminder() }) {
                        Text("知道了")
                    }
                }
            )
        }
    }
}

@Composable
@Preview(backgroundColor = 0xFF101828)
fun VideoRtcPreview() {
    VideoRtcView(channelName = "preview_channel", courseTeacherId = 1L)
}
