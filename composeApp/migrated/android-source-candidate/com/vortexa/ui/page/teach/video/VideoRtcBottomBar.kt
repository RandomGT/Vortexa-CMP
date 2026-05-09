package com.vortexa.ui.page.teach.video

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.util.ToastUtil
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res

/** 底部栏状态：麦克风/扬声器为禁止(静音)或放开(开启) */
data class VideoRtcBottomBarState(
    val micMuted: Boolean = true,
    val speakerMuted: Boolean = true
)

/**
 * 1对1 视频直播页底部按钮组（Figma 283-31032 禁止态 / 283-31114 放开态）。
 * 从左到右：麦克风、扬声器、挂断、屏幕共享、录制。
 *
 * @param state 麦克风/扬声器静音状态
 * @param screenSharing 是否正在屏幕共享
 * @param onMicClick 麦克风点击（由上层处理录音权限后再调 ViewModel）
 * @param onSpeakerClick 扬声器点击
 * @param onHangupClick 挂断点击
 * @param onScreenShareClick 屏幕共享点击
 */
@Composable
fun VideoRtcBottomBar(
    state: VideoRtcBottomBarState,
    screenSharing: Boolean,
    onMicClick: () -> Unit,
    onSpeakerClick: () -> Unit,
    onHangupClick: () -> Unit,
    onScreenShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hangupRed = Color(0xFFFF3B2F)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 18.dp, end = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 麦克风：禁止态用 icon_microphone_off，放开态用 icon_microphone
        IconButton(
            onClick = onMicClick,
            modifier = Modifier.size(54.dp)
        ) {
            Image(
                painter = painterResource(
                    if (state.micMuted) Res.drawable.icon_microphone_off else Res.drawable.icon_microphone
                ),
                contentDescription = if (state.micMuted) "取消静音" else "静音"
            )
        }
        // 扬声器：禁止态用 icon_mute，放开态用 icon_unmute
        IconButton(
            onClick = onSpeakerClick,
            modifier = Modifier.size(54.dp)
        ) {
            Image(
                painter = painterResource(
                    if (state.speakerMuted) Res.drawable.icon_mute else Res.drawable.icon_unmute
                ),
                contentDescription = if (state.speakerMuted) "取消静音" else "静音"
            )
        }
        // 挂断：红色圆角按钮，80dp 宽
        IconButton(
            onClick = {

            },
            modifier = Modifier
                .width(80.dp)
                .padding(horizontal = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(hangupRed, RoundedCornerShape(24.dp))
                    .click {
                        onHangupClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.icon_phone),
                    contentDescription = "挂断",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        // 屏幕共享
        IconButton(
            onClick = onScreenShareClick,
            modifier = Modifier.size(54.dp)
        ) {
            Image(
                painter = if (screenSharing) {
                    painterResource(Res.drawable.icon_share_screen_green)
                }else{
                    painterResource(Res.drawable.icon_share_screen)
                },
                contentDescription = "屏幕共享",

            )
        }
        // 录制
        IconButton(
            onClick = {
                ToastUtil.show("该功能暂未开放，敬请期待")
            },
            modifier = Modifier.size(54.dp)
        ) {
            Image(
                painter = painterResource(Res.drawable.icon_record),
                contentDescription = "录制"
            )
        }
    }
}

@Composable
@Preview(backgroundColor = 0xFF101828)
private fun VideoRtcBottomBarDisabledPreview() {
    VideoRtcBottomBar(
        state = VideoRtcBottomBarState(micMuted = true, speakerMuted = true),
        screenSharing = false,
        onMicClick = {},
        onSpeakerClick = {},
        onHangupClick = {},
        onScreenShareClick = {},
    )
}

