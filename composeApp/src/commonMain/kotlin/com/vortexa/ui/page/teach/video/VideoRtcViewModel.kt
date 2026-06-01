package com.vortexa.ui.page.teach.video

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.config.UserConfig
import com.vortexa.model.RtcChannelUserProfile
import com.vortexa.platform.rtc.RtcEngineController
import com.vortexa.platform.rtc.RtcJoinConfig
import com.vortexa.repository.C2cRepository
import com.vortexa.ui.page.teach.helper.currentTeachingEpochMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val TAG = "VideoRtcViewModel"
private const val TEN_MIN_MS = 10 * 60 * 1000L
private const val MILLIS_PER_DAY = 86_400_000L

class VideoRtcViewModel(
    private val channelNameArg: String,
    private val courseStartTimeMs: Long? = null,
    private val courseEndTimeMs: Long? = null,
    private val courseTeacherId: Long,
    private val c2cRepository: C2cRepository = C2cRepository(),
) : ViewModel() {
    private val _channelName = MutableStateFlow(channelNameArg)
    val channelName: StateFlow<String> = _channelName.asStateFlow()

    private val _token = MutableStateFlow("")
    val token: StateFlow<String> = _token.asStateFlow()

    private val _tokenLoading = MutableStateFlow(true)
    val tokenLoading: StateFlow<Boolean> = _tokenLoading.asStateFlow()

    private val _tokenError = MutableStateFlow<String?>(null)
    val tokenError: StateFlow<String?> = _tokenError.asStateFlow()

    private val _agoraUid = MutableStateFlow(defaultLocalUid())
    val agoraUid: StateFlow<Int> = _agoraUid.asStateFlow()

    private val _targetUid = MutableStateFlow(_agoraUid.value)
    val targetUid: StateFlow<Int> = _targetUid.asStateFlow()

    private val _bottomBarState = MutableStateFlow(VideoRtcBottomBarState(micMuted = true, speakerMuted = false))
    val bottomBarState: StateFlow<VideoRtcBottomBarState> = _bottomBarState.asStateFlow()

    private val _screenSharing = MutableStateFlow(false)
    val screenSharing: StateFlow<Boolean> = _screenSharing.asStateFlow()

    private val _joinedUsers = MutableStateFlow<List<VideoRtcUser>>(listOfNotNull(buildLocalUserOrNull()))
    val joinedUsers: StateFlow<List<VideoRtcUser>> = _joinedUsers.asStateFlow()

    private val pageEnterTimeMs = currentTeachingEpochMillis()
    private val elapsedAnchorMs = courseStartTimeMs ?: pageEnterTimeMs
    private val _elapsedTimeText = MutableStateFlow(initialDurationBarText(currentTeachingEpochMillis()))
    val elapsedTimeText: StateFlow<String> = _elapsedTimeText.asStateFlow()

    private val _lastTenMinutesReminder = MutableStateFlow(false)
    val lastTenMinutesReminder: StateFlow<Boolean> = _lastTenMinutesReminder.asStateFlow()

    private val _courseTimeEnded = MutableStateFlow(false)
    val courseTimeEnded: StateFlow<Boolean> = _courseTimeEnded.asStateFlow()

    private var timerJob: Job? = null
    private var controllerJob: Job? = null
    private var controllerErrorJob: Job? = null
    private var attachedController: RtcEngineController? = null
    private var joinedTokenKey: String? = null
    private var hasShownTenMinuteWarning = false
    private val rtcUserProfileCache = mutableMapOf<Int, RtcChannelUserProfile>()
    private val requestedRtcProfileUidSet = mutableSetOf<Int>()

    init {
        startDurationTicker()
        fetchToken()
    }

    fun attachRtcController(controller: RtcEngineController) {
        if (attachedController === controller) return
        attachedController = controller
        controllerJob?.cancel()
        controllerErrorJob?.cancel()
        controllerJob = viewModelScope.launch {
            combine(controller.joinedUserUids, controller.localUid) { joinedUids, localUid ->
                joinedUids to localUid
            }.collect { (joinedUids, localUid) ->
                syncRtcUserState(joinedUids, localUid)
                ensureRtcUserProfiles(joinedUids, localUid)
            }
        }
        controllerErrorJob = viewModelScope.launch {
            controller.error.collect { message ->
                if (!message.isNullOrBlank()) {
                    _tokenError.value = message
                    _tokenLoading.value = false
                }
            }
        }
    }

    fun joinRtc(controller: RtcEngineController) {
        val tokenValue = _token.value
        val uid = _agoraUid.value
        if (tokenValue.isBlank() || channelNameArg.isBlank() || uid <= 0) return
        val key = "$channelNameArg:$uid:$tokenValue"
        if (joinedTokenKey == key) return
        joinedTokenKey = key
        controller.join(
            RtcJoinConfig(
                token = tokenValue,
                channelName = channelNameArg,
                uid = uid,
            )
        )
        syncRtcUserState(listOf(uid), uid)
        controller.setMicMuted(_bottomBarState.value.micMuted)
        controller.setSpeakerMuted(_bottomBarState.value.speakerMuted)
    }

    fun setMicMuted(muted: Boolean) {
        val old = _bottomBarState.value
        _bottomBarState.value = old.copy(micMuted = muted)
        attachedController?.setMicMuted(muted)
    }

    fun toggleSpeakerMuted() {
        val old = _bottomBarState.value
        val next = old.copy(speakerMuted = !old.speakerMuted)
        _bottomBarState.value = next
        attachedController?.setSpeakerMuted(next.speakerMuted)
    }

    fun switchTargetUid(uid: Int) {
        if (uid > 0) _targetUid.value = uid
    }

    fun dismissLastTenMinutesReminder() {
        _lastTenMinutesReminder.value = false
    }

    fun leaveRtc() {
        attachedController?.leave()
    }

    private fun fetchToken() {
        viewModelScope.launch {
            if (channelNameArg.isBlank()) {
                _tokenLoading.value = false
                _tokenError.value = "无法进入课程，缺少频道信息"
                return@launch
            }
            _tokenLoading.value = true
            _tokenError.value = null
            c2cRepository.getC2cToken(channelNameArg)
                .onSuccess { rtcToken ->
                    _token.value = rtcToken
                    _tokenLoading.value = false
                    Log.d(TAG, "fetchToken success, channelName=$channelNameArg")
                }
                .onFailure { error ->
                    _tokenLoading.value = false
                    _tokenError.value = error.message ?: "获取 Token 失败"
                    Log.w(TAG, "fetchToken fail, channelName=$channelNameArg", error)
                }
        }
    }

    private fun syncRtcUserState(joinedAgoraUids: List<Int>, localAgoraUid: Int) {
        val localUid = if (localAgoraUid > 0) localAgoraUid else _agoraUid.value
        if (localUid > 0) _agoraUid.value = localUid
        val ordered = buildOrderedAgoraUidList(joinedAgoraUids, localUid)
        val remoteUidSet = ordered.filter { it != localUid }.toSet()
        rtcUserProfileCache.keys.retainAll(remoteUidSet)
        requestedRtcProfileUidSet.retainAll(remoteUidSet)
        _targetUid.value = ordered.firstOrNull { it != localUid } ?: localUid
        val localIsTutor = localUserIsTutorForCourse()
        _joinedUsers.value = ordered.mapNotNull { uid ->
            when {
                uid == localUid -> buildLocalUserOrNull()
                else -> rtcUserProfileCache[uid]?.let { buildRemoteRtcUser(uid, it, localIsTutor) }
                    ?: buildRemotePlaceholderUser(uid, localIsTutor)
            }
        }
    }

    private fun ensureRtcUserProfiles(joinedAgoraUids: List<Int>, localAgoraUid: Int) {
        val localUid = if (localAgoraUid > 0) localAgoraUid else _agoraUid.value
        buildOrderedAgoraUidList(joinedAgoraUids, localUid)
            .filter { it > 0 && it != localUid }
            .forEach { uid ->
                if (rtcUserProfileCache.containsKey(uid) || requestedRtcProfileUidSet.contains(uid)) return@forEach
                requestedRtcProfileUidSet += uid
                requestRtcUserProfile(uid)
            }
    }

    private fun requestRtcUserProfile(agoraUid: Int) {
        viewModelScope.launch {
            c2cRepository.getRtcChannelUserProfile(agoraUid)
                .onSuccess { profile ->
                    rtcUserProfileCache[agoraUid] = profile
                    syncRtcUserState(attachedController?.joinedUserUids?.value.orEmpty(), _agoraUid.value)
                }
                .onFailure { error ->
                    Log.w(TAG, "requestRtcUserProfile fail: agoraUid=$agoraUid", error)
                }
        }
    }

    private fun buildOrderedAgoraUidList(joinedAgoraUids: List<Int>, localAgoraUid: Int): List<Int> {
        val remoteAgoraUids = joinedAgoraUids.filter { it > 0 && it != localAgoraUid }.distinct()
        return if (localAgoraUid > 0) listOf(localAgoraUid) + remoteAgoraUids else remoteAgoraUids
    }

    private fun localUserIsTutorForCourse(): Boolean {
        val myTeacherId = UserConfig.getTeacherId()
        return myTeacherId > 0L && myTeacherId == courseTeacherId
    }

    private fun resolveIsTutor(profileTeacherId: Long?, localIsTutorForCourse: Boolean): Boolean {
        return when {
            profileTeacherId == null -> !localIsTutorForCourse
            profileTeacherId <= 0L -> false
            else -> profileTeacherId == courseTeacherId
        }
    }

    private fun buildLocalUserOrNull(): VideoRtcUser? {
        val uid = _agoraUid.value
        if (uid <= 0) return null
        return VideoRtcUser(
            agoraUid = uid,
            name = UserConfig.getNickname().orEmpty().ifBlank { "当前用户" },
            isTutor = localUserIsTutorForCourse(),
            avatarUrl = UserConfig.getAvatar(),
        )
    }

    private fun buildRemotePlaceholderUser(agoraUid: Int, localIsTutorForCourse: Boolean): VideoRtcUser =
        VideoRtcUser(
            agoraUid = agoraUid,
            name = "用户$agoraUid",
            isTutor = !localIsTutorForCourse,
            avatarUrl = null,
        )

    private fun buildRemoteRtcUser(
        agoraUid: Int,
        profile: RtcChannelUserProfile,
        localIsTutorForCourse: Boolean,
    ): VideoRtcUser =
        VideoRtcUser(
            agoraUid = agoraUid,
            name = profile.nickName.orEmpty().ifBlank { "用户$agoraUid" },
            isTutor = resolveIsTutor(profile.teacherId, localIsTutorForCourse),
            avatarUrl = profile.avatar,
        )

    private fun startDurationTicker() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val end = courseEndTimeMs
            while (true) {
                val now = currentTeachingEpochMillis()
                val start = courseStartTimeMs
                _elapsedTimeText.value = when {
                    start != null && now < start -> formatBeforeCourseStartLabel(start)
                    else -> formatElapsed(((now - elapsedAnchorMs) / 1000L).coerceAtLeast(0L))
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

    private fun initialDurationBarText(now: Long): String {
        val start = courseStartTimeMs
        if (start != null && now < start) return formatBeforeCourseStartLabel(start)
        return formatElapsed(((now - elapsedAnchorMs) / 1000L).coerceAtLeast(0L))
    }

    private fun formatBeforeCourseStartLabel(startMs: Long): String =
        "课程开始时间：" + formatEpochMinute(startMs)

    private fun formatElapsed(totalSeconds: Long): String {
        val h = totalSeconds / 3600L
        val m = (totalSeconds % 3600L) / 60L
        val s = totalSeconds % 60L
        return "${h.twoDigits()}:${m.twoDigits()}:${s.twoDigits()}"
    }

    override fun onCleared() {
        timerJob?.cancel()
        controllerJob?.cancel()
        controllerErrorJob?.cancel()
        attachedController?.leave()
        super.onCleared()
    }
}

private fun defaultLocalUid(): Int {
    val userId = UserConfig.getUserId()
    return if (userId in 1..Int.MAX_VALUE.toLong()) userId.toInt() else 0
}

private fun formatEpochMinute(epochMs: Long): String {
    val epochDay = floorDiv(epochMs, MILLIS_PER_DAY)
    val millisOfDay = floorMod(epochMs, MILLIS_PER_DAY)
    val date = epochDaysToCivil(epochDay)
    val hour = (millisOfDay / 3_600_000L).toInt()
    val minute = ((millisOfDay % 3_600_000L) / 60_000L).toInt()
    return "${date.year.toString().padStart(4, '0')}-${date.month.twoDigits()}-${date.day.twoDigits()} ${hour.twoDigits()}:${minute.twoDigits()}"
}

private data class SimpleDate(val year: Int, val month: Int, val day: Int)

private fun epochDaysToCivil(epochDays: Long): SimpleDate {
    var z = epochDays + 719468L
    val era = floorDiv(z, 146097L)
    val doe = z - era * 146097L
    val yoe = (doe - doe / 1460L + doe / 36524L - doe / 146096L) / 365L
    var y = yoe + era * 400L
    val doy = doe - (365L * yoe + yoe / 4L - yoe / 100L)
    val mp = (5L * doy + 2L) / 153L
    val d = doy - (153L * mp + 2L) / 5L + 1L
    val m = mp + if (mp < 10L) 3L else -9L
    y += if (m <= 2L) 1L else 0L
    return SimpleDate(y.toInt(), m.toInt(), d.toInt())
}

private fun Long.twoDigits(): String = toString().padStart(2, '0')
private fun Int.twoDigits(): String = toString().padStart(2, '0')

private fun floorDiv(value: Long, divisor: Long): Long {
    var result = value / divisor
    if ((value xor divisor) < 0 && result * divisor != value) result--
    return result
}

private fun floorMod(value: Long, divisor: Long): Long {
    val mod = value % divisor
    return if (mod < 0) mod + divisor else mod
}
