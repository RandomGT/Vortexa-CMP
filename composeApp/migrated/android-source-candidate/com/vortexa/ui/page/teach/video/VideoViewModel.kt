package com.vortexa.ui.page.teach.video

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.util.DisplayMetrics
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vortexa.config.UserConfig
import com.vortexa.model.RtcChannelUserProfile
import com.vortexa.repository.C2cRepository
import com.vortexa.rtc.RtcEngineHelper
import com.vortexa.rtc.ScreenCapturePermissionResult
import com.vortexa.util.findActivity
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.ScreenCaptureParameters
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "VideoViewModel"

private const val TEN_MIN_MS = 10 * 60 * 1000L

/**
 * 1对1 视频页 ViewModel。接收 [channelName]，请求声网 Token 后延迟初始化 RtcEngine。
 *
 * @param courseTeacherId 本节课导师业务 ID；与用户资料接口返回的 [RtcChannelUserProfile.teacherId] 一致则展示为导师。
 * @param courseEndTimeMs 课程结束时间（epoch 毫秒）；null 时不做下课前提醒与强制关页。
 */
class VideoViewModel(
    private val channelNameArg: String,
    /** 课程开始时间（epoch 毫秒）；null 表示顶栏从进入页面时刻起正计时 */
    private val courseStartTimeMs: Long? = null,
    /** 课程结束时间（epoch 毫秒）；有值时到达结束前 10 分钟弹窗提示，到达结束时间触发 [courseTimeEnded] */
    private val courseEndTimeMs: Long? = null,
    private val courseTeacherId: Long
) : ViewModel() {

    private val courseStartLabelFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

    private val c2cRepository = C2cRepository()

    private val _channelFlow = MutableStateFlow(channelNameArg)

    /** 频道名 */
    val channelName: StateFlow<String> = _channelFlow.asStateFlow()

    private val _tokenFlow = MutableStateFlow("")

    /** 声网 Token，获取成功后非空 */
    val token: StateFlow<String> = _tokenFlow.asStateFlow()

    private val _tokenLoading = MutableStateFlow(true)

    /** Token 请求中，展示 Loading 直到获取成功 */
    val tokenLoading: StateFlow<Boolean> = _tokenLoading.asStateFlow()

    private val _tokenError = MutableStateFlow<String?>(null)

    /** Token 请求失败时的错误信息 */
    val tokenError: StateFlow<String?> = _tokenError.asStateFlow()

    private val _agoraUidFlow = MutableStateFlow(0)
    val agoraUid: StateFlow<Int> = _agoraUidFlow.asStateFlow()

    private val _rtcEngineFlow = MutableStateFlow<RtcEngine?>(null)
    val rtcEngine: StateFlow<RtcEngine?> = _rtcEngineFlow.asStateFlow()

    /** 麦克风默认关闭；扬声器默认开启（非静音） */
    private val _bottomBarStateFlow = MutableStateFlow(VideoRtcBottomBarState(micMuted = true, speakerMuted = false))

    val bottomBarState: StateFlow<VideoRtcBottomBarState> = _bottomBarStateFlow.asStateFlow()

    private val _screenSharingStateFlow = MutableStateFlow(false)

    val screenSharingState: StateFlow<Boolean> = _screenSharingStateFlow.asStateFlow()

    private val _targetUidFlow = MutableStateFlow(0)
    val targetUid: StateFlow<Int> = _targetUidFlow.asStateFlow()

    private val _joinedUsersFlow = MutableStateFlow<List<VideoRtcUser>>(emptyList())

    /** 当前频道内的真实成员列表，始终保证当前用户在首位。 */
    val joinedUsers: StateFlow<List<VideoRtcUser>> = _joinedUsersFlow.asStateFlow()

    /** 未传 [courseStartTimeMs] 时，正计时从进入页面的时刻起算 */
    private val pageEnterTimeMs = System.currentTimeMillis()

    /** 正计时的零点：开课时间或进入页面时刻 */
    private val elapsedAnchorMs: Long =
        courseStartTimeMs ?: pageEnterTimeMs

    private val _elapsedTimeText = MutableStateFlow(
        initialDurationBarText(System.currentTimeMillis())
    )

    /**
     * 顶栏时长区文案：未到开课则为「课程开始时间：yyyy-MM-dd HH:mm」，否则为从开课（或进入页）起算的 HH:MM:SS。
     */
    val elapsedTimeText: StateFlow<String> = _elapsedTimeText.asStateFlow()

    private val _lastTenMinutesReminder = MutableStateFlow(false)

    /** 为 true 时展示「课程剩余最后10分钟」提示（仅一次，可 dismiss） */
    val lastTenMinutesReminder: StateFlow<Boolean> = _lastTenMinutesReminder.asStateFlow()

    private val _courseTimeEnded = MutableStateFlow(false)

    /** 为 true 时 Activity 应结束并通知上一页刷新 */
    val courseTimeEnded: StateFlow<Boolean> = _courseTimeEnded.asStateFlow()

    private var hasShownTenMinuteWarning = false

    private var timerJob: Job? = null

    /** 扬声器静音前的媒体音量，用于恢复；-1 表示未保存过 */
    private var volumeBeforeSpeakerMute: Int = -1
    private val rtcUserProfileCache = mutableMapOf<Int, RtcChannelUserProfile>()
    private val requestedRtcProfileUidSet = mutableSetOf<Int>()

    /** 已调用 [RtcEngine.startScreenCapture] 且正在等待系统录屏授权回调 */
    private val awaitingScreenCapturePermission = AtomicBoolean(false)

    init {
        startDurationTicker()
        fetchToken()
        observeRtcUsers()
        viewModelScope.launch {
            RtcEngineHelper.screenCapturePermissionResults.collect { result ->
                if (!awaitingScreenCapturePermission.compareAndSet(true, false)) {
                    return@collect
                }
                when (result) {
                    ScreenCapturePermissionResult.Granted -> {
                        _screenSharingStateFlow.value = true
                        applyScreenShareChannelPublish(enabled = true)
                    }
                    ScreenCapturePermissionResult.Denied -> {
                        _screenSharingStateFlow.value = false
                        stopScreenCaptureAndRestoreCamera()
                    }
                }
            }
        }
    }

    private fun initialDurationBarText(now: Long): String {
        val start = courseStartTimeMs
        if (start != null && now < start) {
            return formatBeforeCourseStartLabel(start)
        }
        return formatElapsed(((now - elapsedAnchorMs) / 1000).toInt().coerceAtLeast(0))
    }

    private fun formatBeforeCourseStartLabel(startMs: Long): String =
        "课程开始时间：" + courseStartLabelFormatter.format(Instant.ofEpochMilli(startMs))

    /** 请求声网 Token，成功后更新 [token]，失败时设置 [tokenError] */
    private fun fetchToken() {
        viewModelScope.launch {
            _tokenLoading.value = true
            _tokenError.value = null
            c2cRepository.getC2cToken(channelNameArg)
                .onSuccess { t ->
                    _tokenFlow.value = t
                    _tokenLoading.value = false
                    Log.d(TAG, "fetchToken success, channelName=$channelNameArg")
                }
                .onFailure { e ->
                    _tokenLoading.value = false
                    _tokenError.value = e.message ?: "获取 Token 失败"
                    Log.w(TAG, "fetchToken fail, channelName=$channelNameArg", e)
                }
        }
    }

    /**
     * 初始化声网 RtcEngine，应在 Token 获取成功后调用。
     * @param context 用于创建 RtcEngine
     */
    fun generateRtcEngine(context: Context) {
        if (_tokenFlow.value.isBlank()) {
            Log.w(TAG, "generateRtcEngine: token is blank, skip")
            return
        }
        _rtcEngineFlow.value = RtcEngineHelper.getDefaultConfig(context)
        Log.d(TAG, "generateRtcEngine: created, channelName=$channelNameArg")
    }

    /**
     * 监听声网频道成员列表与本地 uid 变化，并同步更新页面上的用户列表与主画面 uid。
     */
    private fun observeRtcUsers() {
        viewModelScope.launch {
            combine(
                RtcEngineHelper.joinedUserUidListFlow,
                RtcEngineHelper.localUidFlow
            ) { joinedAgoraUids, localAgoraUid ->
                joinedAgoraUids to localAgoraUid
            }.collect { (joinedAgoraUids, localAgoraUid) ->
                Log.i(
                    TAG,
                    "observeRtcUsers: localAgoraUid=$localAgoraUid, joinedAgoraUids=$joinedAgoraUids"
                )
                syncRtcUserState(
                    joinedAgoraUids = joinedAgoraUids,
                    localAgoraUid = localAgoraUid
                )
                ensureRtcUserProfiles(
                    joinedAgoraUids = joinedAgoraUids,
                    localAgoraUid = localAgoraUid
                )
            }
        }
    }

    /**
     * 根据声网 uid 列表构建页面展示状态，并保证当前用户始终排在第一位。
     * @param joinedAgoraUids 当前频道中的全部声网 uid
     * @param localAgoraUid 当前设备对应的声网 uid
     */
    private fun syncRtcUserState(
        joinedAgoraUids: List<Int>,
        localAgoraUid: Int
    ) {
        val orderedAgoraUids = buildOrderedAgoraUidList(joinedAgoraUids, localAgoraUid)
        val localIsTutorForCourse = localUserIsTutorForCourse()
        val activeRemoteAgoraUidSet = orderedAgoraUids.filter { it != localAgoraUid }.toSet()
        rtcUserProfileCache.keys.retainAll(activeRemoteAgoraUidSet)
        requestedRtcProfileUidSet.retainAll(activeRemoteAgoraUidSet)
        _agoraUidFlow.value = localAgoraUid
        _targetUidFlow.value = orderedAgoraUids.firstOrNull { it != localAgoraUid } ?: localAgoraUid
        _joinedUsersFlow.value = orderedAgoraUids.map { agoraUid ->
            when {
                agoraUid == localAgoraUid && agoraUid > 0 -> buildLocalRtcUser(
                    agoraUid = agoraUid
                )

                else -> {
                    val cachedProfile = rtcUserProfileCache[agoraUid]
                    if (cachedProfile != null) {
                        buildRemoteRtcUser(
                            agoraUid = agoraUid,
                            profile = cachedProfile,
                            localIsTutorForCourse = localIsTutorForCourse
                        )
                    } else {
                        buildRemotePlaceholderUser(
                            agoraUid = agoraUid,
                            localIsTutorForCourse = localIsTutorForCourse
                        )
                    }
                }
            }
        }
        Log.d(TAG, "syncRtcUserState: targetUid=${_targetUidFlow.value}, users=${_joinedUsersFlow.value}")
        if (localAgoraUid > 0) {
            applyMicPublishStateToEngine()
        }
    }

    /**
     * 为尚未拿到业务资料的远端用户发起查询，查询前先用默认头像和默认名称占位。
     * @param joinedAgoraUids 当前频道中的全部声网 uid
     * @param localAgoraUid 当前设备对应的声网 uid
     */
    private fun ensureRtcUserProfiles(
        joinedAgoraUids: List<Int>,
        localAgoraUid: Int
    ) {
        buildOrderedAgoraUidList(joinedAgoraUids, localAgoraUid)
            .filter { it != localAgoraUid }
            .forEach { agoraUid ->
                if (rtcUserProfileCache.containsKey(agoraUid) || requestedRtcProfileUidSet.contains(agoraUid)) {
                    return@forEach
                }
                requestedRtcProfileUidSet += agoraUid
                requestRtcUserProfile(agoraUid)
            }
    }

    /**
     * 查询远端声网用户对应的业务资料，查询成功后刷新列表。
     * @param agoraUid 远端用户的声网 uid
     */
    private fun requestRtcUserProfile(agoraUid: Int) {
        viewModelScope.launch {
            Log.i(TAG, "requestRtcUserProfile: agoraUid=$agoraUid")
            c2cRepository.getRtcChannelUserProfile(agoraUid)
                .onSuccess { profile ->
                    rtcUserProfileCache[agoraUid] = profile
                    Log.i(TAG, "requestRtcUserProfile success: agoraUid=$agoraUid, profile=$profile")
                    syncRtcUserState(
                        joinedAgoraUids = RtcEngineHelper.joinedUserUidListFlow.value,
                        localAgoraUid = RtcEngineHelper.localUidFlow.value
                    )
                }
                .onFailure { throwable ->
                    Log.w(TAG, "requestRtcUserProfile fail: agoraUid=$agoraUid", throwable)
                }
        }
    }

    /**
     * 构建页面展示顺序，确保本地用户始终位于首位，其余远端用户保持原有加入顺序。
     * @param joinedAgoraUids 当前频道中的全部声网 uid
     * @param localAgoraUid 当前设备对应的声网 uid
     * @return 已排序的展示 uid 列表
     */
    private fun buildOrderedAgoraUidList(
        joinedAgoraUids: List<Int>,
        localAgoraUid: Int
    ): List<Int> {
        val remoteAgoraUids = joinedAgoraUids.filter { it != localAgoraUid }.distinct()
        return if (localAgoraUid > 0) {
            listOf(localAgoraUid) + remoteAgoraUids
        } else {
            remoteAgoraUids
        }
    }

    /** 当前登录用户是否为本节课导师（本地缓存的导师 ID 与进房传入的 [courseTeacherId] 一致） */
    private fun localUserIsTutorForCourse(): Boolean {
        val myTid = UserConfig.getTeacherId()
        return myTid > 0L && myTid == courseTeacherId
    }

    /**
     * 是否展示为导师：以资料接口 [RtcChannelUserProfile.teacherId] 与 [courseTeacherId] 为准；
     * 未返回 [profileTeacherId] 时用 1 对 1 另一方角色兜底。
     */
    private fun resolveIsTutorFromProfileTeacherId(
        profileTeacherId: Long?,
        localIsTutorForCourse: Boolean
    ): Boolean {
        return when {
            profileTeacherId == null -> !localIsTutorForCourse
            profileTeacherId <= 0L -> false
            else -> profileTeacherId == courseTeacherId
        }
    }

    /**
     * 构建当前用户的展示信息，优先使用本地已登录用户的昵称和头像。
     * @param agoraUid 当前设备对应的声网 uid
     * @return 视频页用户列表项
     */
    private fun buildLocalRtcUser(
        agoraUid: Int
    ): VideoRtcUser {
        return VideoRtcUser(
            agoraUid = agoraUid,
            name = UserConfig.getNickname().orEmpty().ifBlank { "当前用户" },
            isTutor = localUserIsTutorForCourse(),
            avatarUrl = UserConfig.getAvatar()
        )
    }

    /**
     * 构建远端用户的默认占位信息，在业务资料返回前先展示默认头像和名称。
     * @param agoraUid 远端用户的声网 uid
     * @param localIsTutorForCourse 当前用户是否本节课导师，用于推导对端角色
     * @return 占位列表项
     */
    private fun buildRemotePlaceholderUser(
        agoraUid: Int,
        localIsTutorForCourse: Boolean
    ): VideoRtcUser {
        return VideoRtcUser(
            agoraUid = agoraUid,
            name = "用户$agoraUid",
            isTutor = !localIsTutorForCourse,
            avatarUrl = null
        )
    }

    /**
     * 将远端用户的业务资料映射为页面展示模型。
     * @param agoraUid 远端用户的声网 uid
     * @param profile 业务接口返回的用户资料
     * @param localIsTutorForCourse 当前用户是否本节课导师，在接口未下发 teacherId 时兜底
     * @return 带真实昵称和头像的列表项
     */
    private fun buildRemoteRtcUser(
        agoraUid: Int,
        profile: RtcChannelUserProfile,
        localIsTutorForCourse: Boolean
    ): VideoRtcUser {
        return VideoRtcUser(
            agoraUid = agoraUid,
            name = profile.nickName.orEmpty().ifBlank { "用户$agoraUid" },
            isTutor = resolveIsTutorFromProfileTeacherId(profile.teacherId, localIsTutorForCourse),
            avatarUrl = profile.avatar
        )
    }

    /**
     * 顶栏计时：每秒刷新；未到开课显示开始时间，到点后从 [elapsedAnchorMs] 正计时。离开页面后 [viewModelScope] 取消，[onCleared] 再取消 [timerJob]。
     */
    private fun startDurationTicker() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val end = courseEndTimeMs
            while (true) {
                val now = System.currentTimeMillis()
                val start = courseStartTimeMs
                _elapsedTimeText.value = when {
                    start != null && now < start -> formatBeforeCourseStartLabel(start)
                    else -> {
                        val elapsedSec = ((now - elapsedAnchorMs) / 1000).toInt().coerceAtLeast(0)
                        formatElapsed(elapsedSec)
                    }
                }
                if (end != null) {
                    if (now >= end) {
                        _courseTimeEnded.value = true
                        break
                    }
                    if (!hasShownTenMinuteWarning && now >= end - TEN_MIN_MS) {
                        hasShownTenMinuteWarning = true
                        _lastTenMinutesReminder.value = true
                    }
                }
                delay(1000L)
            }
        }
    }

    fun dismissLastTenMinutesReminder() {
        _lastTenMinutesReminder.value = false
    }

    /**
     * 将秒数格式化为 "HH:MM:SS"，不足两位补 0。
     * @param totalSeconds 总秒数
     * @return 如 "01:35:44"
     */
    private fun formatElapsed(totalSeconds: Int): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return "%02d:%02d:%02d".format(h, m, s)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        awaitingScreenCapturePermission.set(false)
    }

    /**
     * 设置本地麦克风静音（关闭）或采集并发布（开启）。
     * 开启前需已由 UI 层确认 [android.Manifest.permission.RECORD_AUDIO] 已授予。
     */
    fun setMicMuted(muted: Boolean) {
        val state = _bottomBarStateFlow.value
        if (state.micMuted == muted) {
            applyMicPublishStateToEngine()
            return
        }
        _bottomBarStateFlow.value = VideoRtcBottomBarState(muted, state.speakerMuted)
        applyMicPublishStateToEngine()
    }

    /**
     * 按当前 [VideoRtcBottomBarState.micMuted] 同步声网：闭麦时不发布麦克风轨道，开麦时发布并取消 mute。
     */
    private fun applyMicPublishStateToEngine() {
        val engine = _rtcEngineFlow.value ?: return
        if (_agoraUidFlow.value <= 0) return
        val muted = _bottomBarStateFlow.value.micMuted
        if (muted) {
            engine.muteLocalAudioStream(true)
            engine.updateChannelMediaOptions(
                ChannelMediaOptions().apply {
                    publishMicrophoneTrack = false
                }
            )
        } else {
            engine.updateChannelMediaOptions(
                ChannelMediaOptions().apply {
                    publishMicrophoneTrack = true
                }
            )
            engine.muteLocalAudioStream(false)
        }
    }

    /**
     * 扬声器静音/取消静音：根据 [state.speakerMuted] 静音时保存并置 0 媒体音量，取消静音时恢复原音量。
     * @param context 用于获取 [AudioManager]，建议传 [androidx.compose.ui.platform.LocalContext]
     */
    fun onSpeakerMuteClick(context: Context) {
        val state = _bottomBarStateFlow.value
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: run {
                Log.w("VideoViewModel", "onSpeakerMuteClick: AudioManager not available")
                _bottomBarStateFlow.value = VideoRtcBottomBarState(state.micMuted, !state.speakerMuted)
                return
            }
        if (state.speakerMuted) {
            // 取消静音：恢复静音前的音量，若未保存过则用最大音量一半
            val target = if (volumeBeforeSpeakerMute >= 0) volumeBeforeSpeakerMute
            else (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2).coerceAtLeast(1)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
            volumeBeforeSpeakerMute = -1
        } else {
            // 静音：保存当前音量并设为 0
            volumeBeforeSpeakerMute = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
        }
        _bottomBarStateFlow.value = VideoRtcBottomBarState(state.micMuted, !state.speakerMuted)
    }

    /**
     * 挂断电话
     */
    fun onHangupClick(context: Context) {
        (context as? Activity)?.finish()
    }

    /**
     * 开启或关闭屏幕共享。开启时仅在声网 [IRtcEngineEventHandler.onPermissionGranted]（录屏授权）成功后
     * 才将 [screenSharingState] 置为 true，避免授权弹窗前底部按钮误高亮。
     */
    fun requestScreenSharing(context: Context) {
        if (awaitingScreenCapturePermission.get() && !_screenSharingStateFlow.value) {
            Log.d(TAG, "requestScreenSharing: already awaiting screen capture permission")
            return
        }
        if (_screenSharingStateFlow.value) {
            awaitingScreenCapturePermission.set(false)
            _screenSharingStateFlow.value = false
            stopScreenCaptureAndRestoreCamera()
            return
        }
        val engine = rtcEngine.value ?: run {
            Log.w(TAG, "requestScreenSharing: engine is null")
            return
        }
        val metrics = DisplayMetrics()
        context.findActivity().windowManager.defaultDisplay.getRealMetrics(metrics)
        val screenCaptureParameters = ScreenCaptureParameters()
        screenCaptureParameters.captureVideo = true
        screenCaptureParameters.videoCaptureParameters.width = 720
        screenCaptureParameters.videoCaptureParameters.height =
            (720 * 1.0f / metrics.widthPixels.coerceAtLeast(1) * metrics.heightPixels).toInt()
        screenCaptureParameters.videoCaptureParameters.framerate = 15
        awaitingScreenCapturePermission.set(true)
        val ret = engine.startScreenCapture(screenCaptureParameters)
        if (ret < 0) {
            awaitingScreenCapturePermission.set(false)
            Log.w(TAG, "startScreenCapture failed, ret=$ret")
        }
    }

    private fun applyScreenShareChannelPublish(enabled: Boolean) {
        val engine = rtcEngine.value ?: return
        if (enabled) {
            engine.updateChannelMediaOptions(
                ChannelMediaOptions().apply {
                    publishScreenCaptureVideo = true
                    publishCameraTrack = false
                }
            )
        } else {
            engine.updateChannelMediaOptions(
                ChannelMediaOptions().apply {
                    publishScreenCaptureVideo = false
                    publishCameraTrack = true
                }
            )
        }
    }

    private fun stopScreenCaptureAndRestoreCamera() {
        val engine = rtcEngine.value ?: return
        engine.stopScreenCapture()
        applyScreenShareChannelPublish(enabled = false)
    }
}

/** 用于向 VideoViewModel 传入 channelName、可选开课/结课时间与课程导师 ID */
class VideoViewModelFactory(
    private val channelName: String,
    private val courseStartTimeMs: Long? = null,
    private val courseEndTimeMs: Long? = null,
    private val courseTeacherId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass != VideoViewModel::class.java) {
            throw IllegalArgumentException("VideoViewModelFactory only creates VideoViewModel")
        }
        return VideoViewModel(channelName, courseStartTimeMs, courseEndTimeMs, courseTeacherId) as T
    }
}