package com.vortexa.platform.rtc

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import platform.UIKit.UIColor
import platform.UIKit.UIView

@Composable
actual fun RtcVideoSurface(
    controller: RtcEngineController,
    localUid: Int,
    targetUid: Int,
    screenSharing: Boolean,
    modifier: Modifier,
) {
    UIKitView(
        factory = {
            UIView().apply {
                backgroundColor = UIColor.blackColor
            }
        },
        modifier = modifier,
        update = { view ->
            controller.bindVideoSurface(
                surface = view,
                localUid = localUid,
                targetUid = targetUid,
                screenSharing = screenSharing,
            )
        },
        onRelease = {
            controller.bindVideoSurface(
                surface = null,
                localUid = localUid,
                targetUid = targetUid,
                screenSharing = screenSharing,
            )
        },
    )
}
