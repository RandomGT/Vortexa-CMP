package com.vortexa.platform.rtc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RtcJoinConfig(
    val token: String,
    val channelName: String,
    val uid: Int,
)

interface RtcNativeBridgeFactory {
    fun create(): RtcNativeBridge
}

interface RtcNativeBridge {
    fun join(token: String, channelName: String, uid: Int, eventSink: RtcNativeEventSink)
    fun leave()
    fun setMicMuted(muted: Boolean)
    fun setSpeakerMuted(muted: Boolean)
    fun bindVideoSurface(surface: Any?, localUid: Int, targetUid: Int, screenSharing: Boolean)
    fun dispose()
}

interface RtcNativeEventSink {
    fun onJoinSuccess(localUid: Int)
    fun onRemoteUserJoined(uid: Int)
    fun onRemoteUserLeft(uid: Int)
    fun onError(message: String)
}

object RtcNativeBridgeRegistry {
    private var factory: RtcNativeBridgeFactory? = null

    fun registerFactory(factory: RtcNativeBridgeFactory?) {
        this.factory = factory
    }

    fun createBridge(): RtcNativeBridge? = factory?.create()
}

class RtcEngineController {
    private val nativeBridge: RtcNativeBridge? = RtcNativeBridgeRegistry.createBridge()

    private val _localUid = MutableStateFlow(0)
    val localUid: StateFlow<Int> = _localUid.asStateFlow()

    private val _remoteUids = MutableStateFlow<List<Int>>(emptyList())
    val remoteUids: StateFlow<List<Int>> = _remoteUids.asStateFlow()

    private val _joinedUserUids = MutableStateFlow<List<Int>>(emptyList())
    val joinedUserUids: StateFlow<List<Int>> = _joinedUserUids.asStateFlow()

    private val _joined = MutableStateFlow(false)
    val joined: StateFlow<Boolean> = _joined.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val eventSink = object : RtcNativeEventSink {
        override fun onJoinSuccess(localUid: Int) {
            _localUid.value = localUid
            _joined.value = true
            refreshJoinedUsers()
        }

        override fun onRemoteUserJoined(uid: Int) {
            if (uid <= 0) return
            _remoteUids.value = (_remoteUids.value + uid).distinct()
            refreshJoinedUsers()
        }

        override fun onRemoteUserLeft(uid: Int) {
            _remoteUids.value = _remoteUids.value.filterNot { it == uid }
            refreshJoinedUsers()
        }

        override fun onError(message: String) {
            _error.value = message.ifBlank { "RTC 连接失败" }
        }
    }

    fun join(config: RtcJoinConfig) {
        if (config.token.isBlank() || config.channelName.isBlank() || config.uid <= 0) {
            _error.value = "RTC 参数不完整，无法进入课堂"
            return
        }
        val bridge = nativeBridge
        if (bridge == null) {
            _error.value = "RTC SDK 未完成配置"
            return
        }
        _localUid.value = config.uid
        _error.value = null
        bridge.join(config.token, config.channelName, config.uid, eventSink)
    }

    fun leave() {
        nativeBridge?.leave()
        _joined.value = false
        _remoteUids.value = emptyList()
        refreshJoinedUsers()
    }

    fun setMicMuted(muted: Boolean) {
        nativeBridge?.setMicMuted(muted)
    }

    fun setSpeakerMuted(muted: Boolean) {
        nativeBridge?.setSpeakerMuted(muted)
    }

    fun bindVideoSurface(surface: Any?, localUid: Int, targetUid: Int, screenSharing: Boolean) {
        nativeBridge?.bindVideoSurface(surface, localUid, targetUid, screenSharing)
    }

    fun dispose() {
        leave()
        nativeBridge?.dispose()
    }

    private fun refreshJoinedUsers() {
        _joinedUserUids.value = buildList {
            val local = _localUid.value
            if (local > 0 && _joined.value) add(local)
            addAll(_remoteUids.value.filter { it > 0 && it != local })
        }.distinct()
    }
}

@Composable
fun rememberRtcEngineController(): RtcEngineController {
    val controller = remember { RtcEngineController() }
    DisposableEffect(controller) {
        onDispose { controller.dispose() }
    }
    return controller
}

@Composable
expect fun RtcVideoSurface(
    controller: RtcEngineController,
    localUid: Int,
    targetUid: Int,
    screenSharing: Boolean,
    modifier: Modifier = Modifier,
)
