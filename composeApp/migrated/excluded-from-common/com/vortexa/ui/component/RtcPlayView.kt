package com.vortexa.ui.component

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vortexa.rtc.RtcEngineHelper
import com.vortexa.rtc.isJoined
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas

/**
 * target UID:显示谁的屏幕
 */
@Composable
fun RtcPlayView(
    rtcEngine: RtcEngine,
    token: String,
    channelName: String,
    agoraUid: Int,
    targetUid: Int,
    /** 本地是否正在屏幕共享；为 true 时本地预览需绑定屏幕采集源，否则仍渲染摄像头 */
    screenSharing: Boolean = false,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val rtcEngine = remember { rtcEngine }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        onDispose {
            if (isJoined) {
                rtcEngine.leaveChannel()
            }
            RtcEngine.destroy()
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                val mediaOptions = ChannelMediaOptions().apply {
                    channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
                    clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                    publishCameraTrack = true
                    publishMicrophoneTrack = false
                }
                rtcEngine.joinChannel(token, channelName, agoraUid, mediaOptions)
            } else {
                Toast.makeText(context, "需要相机权限才能进入课堂", Toast.LENGTH_LONG).show()
            }
        }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // 屏幕共享会改变本地 VideoSourceType；VideoCell 仅在 id 变更时 re-setup，故用 key 强制重建绑定
    key(targetUid, agoraUid, screenSharing) {
        VideoCell(
            id = targetUid,
            modifier = Modifier.fillMaxSize(),
            isLocal = false,
            setupVideo = { view, id, _ ->
                val canvas = VideoCanvas(view, Constants.RENDER_MODE_FIT, id)
                if (targetUid == agoraUid) {
                    // VideoCanvas.sourceType 为 int，与 API 文档中 VideoSourceType 数值一致
                    canvas.sourceType = if (screenSharing) {
                        Constants.VideoSourceType.VIDEO_SOURCE_SCREEN_PRIMARY.ordinal
                    } else {
                        Constants.VideoSourceType.VIDEO_SOURCE_CAMERA_PRIMARY.ordinal
                    }
                    rtcEngine.setupLocalVideo(canvas)
                } else {
                    rtcEngine.setupRemoteVideo(canvas)
                }
            },
        )
    }

}
