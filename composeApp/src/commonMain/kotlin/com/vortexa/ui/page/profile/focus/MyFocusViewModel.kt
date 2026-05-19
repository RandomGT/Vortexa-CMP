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
 */
class MyFocusViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val followRepository: FollowRepository = FollowRepository()
) : ViewModel() {

    private val _focusList = MutableStateFlow<List<FocusUser>>(emptyList())
    val focusList: StateFlow<List<FocusUser>> = _focusList.asStateFlow()

    private val _followLoading = MutableStateFlow(false)
    val followLoading: StateFlow<Boolean> = _followLoading.asStateFlow()

    private val _unfollowLoading = MutableStateFlow(false)
    val unfollowLoading: StateFlow<Boolean> = _unfollowLoading.asStateFlow()

    private val _pageStatus = MutableStateFlow(PageStatus.Loading)
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

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

    fun follow(userId: Long) {
        viewModelScope.launch {
            _followLoading.value = true
            userRepository.follow(userId)
                .onSuccess {
                    _focusList.value = _focusList.value.map {
                        if (it.userId == userId) it.copy(isFollowing = true) else it
                    }
                }
                .onFailure { Log.e(TAG, "follow: failed", it) }
            _followLoading.value = false
        }
    }

    fun unfollow(userId: Long) {
        viewModelScope.launch {
            _unfollowLoading.value = true
            userRepository.unfollow(userId)
                .onSuccess {
                    _focusList.value = _focusList.value.map {
                        if (it.userId == userId) it.copy(isFollowing = false) else it
                    }
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

