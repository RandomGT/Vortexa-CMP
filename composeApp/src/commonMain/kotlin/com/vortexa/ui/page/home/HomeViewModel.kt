package com.vortexa.ui.page.home

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.config.TokenConfig
import com.vortexa.config.UserConfig
import com.vortexa.model.UserProfileResponse
import com.vortexa.repository.UserRepository
import com.vortexa.ui.component.pageStatus.PageStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 *  desc : 首页ViewModel
 *
 *
 *  @author LuXin
 *  @createTime 2026/1/19
 */

class HomeViewModel(initialTab: Int = 0) : ViewModel() {
    var currentTab = mutableStateOf(initialTab.coerceIn(0, 4))
    
    private val _pageStatus = MutableStateFlow(PageStatus.Success)
    val pageStatus = _pageStatus.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfileResponse?>(null)
    val userProfile = _userProfile.asStateFlow()
    
    private val userRepository by lazy { UserRepository() }

    init {
        if (TokenConfig.getToken().isNotEmpty()) {
            fetchUserProfile()
        } else {
            Log.d(TAG, "init: 未登录，跳过 fetchUserProfile，避免无效请求")
        }
    }

    fun onTabClick(index: Int) {
        currentTab.value = index
    }
    
    fun fetchUserProfile() {
        if (TokenConfig.getToken().isEmpty()) {
            Log.d(TAG, "fetchUserProfile: 未登录，跳过请求")
            _userProfile.value = null
            _pageStatus.value = PageStatus.Success
            return
        }
        viewModelScope.launch {
            _pageStatus.value = PageStatus.Loading
            val userId = UserConfig.getUserId()
            Log.d(TAG, "fetchUserProfile: userId=$userId")
            
            userRepository.getUserProfile(userId)
                .onSuccess {
                    _userProfile.value = it
                    _pageStatus.value = PageStatus.Success
                }
                .onFailure {
                    Log.e(TAG, "fetchUserProfile error", it)
                    _pageStatus.value = PageStatus.Fail
                }
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}
