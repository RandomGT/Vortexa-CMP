package com.vortexa.ui.page.profile.focus

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.model.FollowingListItem
import com.vortexa.repository.FollowRepository
import com.vortexa.repository.UserRepository
import com.vortexa.ui.component.pageStatus.PageStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 我的关注页 ViewModel。
 * 负责关注列表（GET /v/api/dynamic/followingList）、关注/取消关注及 [PageStatusView] 状态。
 */
class MyFocusViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val followRepository: FollowRepository = FollowRepository()
) : ViewModel() {

    private val _focusList = MutableStateFlow<List<FocusUser>>(emptyList())
    /** 关注列表 */
    val focusList: StateFlow<List<FocusUser>> = _focusList.asStateFlow()

    private val _followLoading = MutableStateFlow(false)
    val followLoading: StateFlow<Boolean> = _followLoading.asStateFlow()

    private val _unfollowLoading = MutableStateFlow(false)
    val unfollowLoading: StateFlow<Boolean> = _unfollowLoading.asStateFlow()

    private val _pageStatus = MutableStateFlow(PageStatus.Loading)
    /** 页面请求状态，供 [com.vortexa.ui.component.pageStatus.PageStatusView] 使用 */
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    /**
     * 加载关注列表。
     *
     * @param showPageLoading 为 true 时进入全屏 Loading（首次进入、失败重试、空态刷新）。
     */
    fun loadFocusList(showPageLoading: Boolean = true) {
        viewModelScope.launch {
            if (showPageLoading) {
                _pageStatus.value = PageStatus.Loading
            }
            followRepository.getFollowingList(pageNum = 1, pageSize = 50)
                .onSuccess { response ->
                    val list = response.list.map { it.toFocusUser() }
                    _focusList.value = list
                    _pageStatus.value = if (list.isEmpty()) PageStatus.Empty else PageStatus.Success
                    Log.d(TAG, "loadFocusList: success, size=${list.size}")
                }
                .onFailure {
                    Log.e(TAG, "loadFocusList: failed", it)
                    _focusList.value = emptyList()
                    _pageStatus.value = PageStatus.Fail
                }
        }
    }

    /**
     * 关注用户。
     */
    fun follow(userId: Long) {
        viewModelScope.launch {
            _followLoading.value = true
            Log.d(TAG, "follow: userId=$userId")
            userRepository.follow(userId)
                .onSuccess {
                    _focusList.value = _focusList.value.map {
                        if (it.userId == userId) it.copy(isFollowing = true) else it
                    }
                    Log.d(TAG, "follow: success")
                }
                .onFailure { Log.e(TAG, "follow: failed", it) }
            _followLoading.value = false
        }
    }

    /**
     * 取消关注用户。
     */
    fun unfollow(userId: Long) {
        viewModelScope.launch {
            _unfollowLoading.value = true
            Log.d(TAG, "unfollow: userId=$userId")
            userRepository.unfollow(userId)
                .onSuccess {
                    _focusList.value = _focusList.value.map {
                        if (it.userId == userId) it.copy(isFollowing = false) else it
                    }
                    Log.d(TAG, "unfollow: success")
                }
                .onFailure { Log.e(TAG, "unfollow: failed", it) }
            _unfollowLoading.value = false
        }
    }

    private fun FollowingListItem.toFocusUser(): FocusUser = FocusUser(
        userId = userId,
        nickname = userName.ifEmpty { "用户" },
        bio = "",
        isFollowing = true
    )

    companion object {
        private const val TAG = "MyFocusVM"
    }
}
