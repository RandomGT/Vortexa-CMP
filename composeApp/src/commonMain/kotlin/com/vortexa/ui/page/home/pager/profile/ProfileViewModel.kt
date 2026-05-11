package com.vortexa.ui.page.home.pager.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.model.UserCenterInfo
import com.vortexa.repository.UserRepository
import com.vortexa.navigation.AppRoute
import com.vortexa.navigation.NavigationRouteBridge
import com.vortexa.navigation.ProfileSubPageKind
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.page.teach.myclass.MyClassActivity
import com.vortexa.util.ToastUtil
import com.vortexa.util.extension.routeToPage
import com.vortexa.config.TokenConfig
import com.vortexa.config.UserConfig
import com.vortexa.lib_net.exception.ApiException
import com.vortexa.net.auth.isLoginRequired
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 个人资料页 ViewModel，负责个人中心数据加载与页面跳转。
 *
 * @author LuXin
 * @createTime 2026/2/27
 */
class ProfileViewModel : ViewModel() {

    private val userRepository by lazy { UserRepository() }

    /** 首屏不在此拉接口，见 [ProfileView] 选中后再 [loadUserCenterInfo]。 */
    private val _pageStatus = MutableStateFlow(PageStatus.Success)
    val pageStatus = _pageStatus.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    /** 下拉刷新指示器是否与接口请求同步 */
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _userCenterInfo = MutableStateFlow<UserCenterInfo?>(null)
    val userCenterInfo = _userCenterInfo.asStateFlow()

    /** 当前用户教师 ID（与个人中心接口同步，并持久化至 [UserConfig]），非教师为 0 */
    private val _teacherId = MutableStateFlow(UserConfig.getTeacherId())
    val teacherId: StateFlow<Long> = _teacherId.asStateFlow()

    /** 编辑资料确认按钮 loading 态 */
    private val _confirmLoading = MutableStateFlow(false)
    val confirmLoading = _confirmLoading.asStateFlow()

    /** 编辑资料成功后需关闭 Modal 并刷新 */
    private val _updateProfileSuccess = MutableStateFlow(false)
    val updateProfileSuccess = _updateProfileSuccess.asStateFlow()

    /**
     * 全屏加载路径：Tab 首次展示、[PageStatusView] 重试时进入 Loading 再拉取个人中心。
     */
    fun loadUserCenterInfo() {
        viewModelScope.launch {
            _pageStatus.value = PageStatus.Loading
            Log.d(TAG, "loadUserCenterInfo: start")
            try {
                fetchUserCenterInfoAndUpdateUi()
            } catch (e: Exception) {
                Log.e(TAG, "loadUserCenterInfo: exception", e)
                _pageStatus.value = PageStatus.Fail
            }
        }
    }

    /**
     * 下拉刷新：仅驱动 [isRefreshing]，不切换全屏 Loading。
     *
     * @param showRefreshing 是否展示下拉刷新圈；用户手势时为 true。
     */
    fun refresh(showRefreshing: Boolean = true) {
        viewModelScope.launch {
            if (showRefreshing && _isRefreshing.value) {
                Log.w(TAG, "refresh ignored: already refreshing")
                return@launch
            }
            if (showRefreshing) {
                _isRefreshing.value = true
            }
            Log.i(TAG, "refresh start, showRefreshing=$showRefreshing")
            try {
                fetchUserCenterInfoAndUpdateUi()
            } catch (e: Exception) {
                Log.e(TAG, "refresh failed", e)
            } finally {
                if (showRefreshing) {
                    _isRefreshing.value = false
                }
                Log.i(TAG, "refresh end")
            }
        }
    }

    /**
     * 请求个人中心并更新 [userCenterInfo]、[pageStatus]（不主动设为 Loading）。
     */
    private suspend fun fetchUserCenterInfoAndUpdateUi() {
        userRepository.getUserCenterInfo()
            .onSuccess {
                _userCenterInfo.value = it
                UserConfig.setTeacherId(it.userInfo.teacherId)
                _teacherId.value = UserConfig.getTeacherId()
                _pageStatus.value = PageStatus.Success
                Log.d(TAG, "fetchUserCenterInfoAndUpdateUi: success, teacherId=${_teacherId.value}")
            }
            .onFailure { e ->
                if (e.isLoginRequired()) {
                    _userCenterInfo.value = null
                    UserConfig.setTeacherId(null)
                    _teacherId.value = 0L
                    _pageStatus.value = PageStatus.Success
                    Log.i(TAG, "fetchUserCenterInfoAndUpdateUi: guest")
                    return@onFailure
                }
                Log.e(TAG, "fetchUserCenterInfoAndUpdateUi: fail", e)
                _pageStatus.value = PageStatus.Fail
            }
    }
    fun jumpToCreator(context: Context) {
        ToastUtil.show(context, "创作中心页面即将上线")
    }

    fun jumpToInteraction(context: Context) {
        NavigationRouteBridge.navigate(AppRoute.ProfileSubPage(ProfileSubPageKind.Interaction))
    }

    fun jumpToCollection(context: Context) {
        NavigationRouteBridge.navigate(AppRoute.ProfileSubPage(ProfileSubPageKind.Collection))
    }

    fun jumpToHistory(context: Context) {
        NavigationRouteBridge.navigate(AppRoute.ProfileSubPage(ProfileSubPageKind.History))
    }

    fun jumpToCourse(context: Context) {
        context.routeToPage(MyClassActivity::class)
    }

    /**
     * 编辑头像/昵称。先上传头像（若有），再调用 update 接口。
     * 成功时发出 updateProfileSuccess，调用方负责关闭 Modal 并刷新。
     *
     * @param userId 当前用户 ID
     * @param userName 新昵称（编辑框当前值）
     * @param avatarUri 新头像本地 Uri，未选择可传 null
     * @param currentUserName 当前昵称，用于判断昵称是否变更
     * @param context 用于 Toast 等
     */
    fun updateUserCenter(
        userId: Long,
        userName: String?,
        avatarUri: Uri?,
        currentUserName: String?,
        context: Context
    ) {
        viewModelScope.launch {
            _confirmLoading.value = true
            try {
                Log.d(TAG, "updateUserCenter: start, userId=$userId")
                var avatarUrl: String? = null
                if (avatarUri != null) {
                    avatarUrl = userRepository.uploadAvatar(avatarUri).getOrElse { e ->
                        Log.e(TAG, "updateUserCenter: upload fail", e)
                        if (e !is ApiException) {
                            Toast.makeText(context, e.message ?: "头像上传失败", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }
                    Log.d(TAG, "updateUserCenter: avatar uploaded, url=$avatarUrl")
                }
                val avatar = avatarUrl
                val nameChanged = userName != null && userName != currentUserName
                val name =
                    if (nameChanged && !userName.isNullOrBlank()) userName else null
                if (avatar == null && name == null) {
                    Log.d(TAG, "updateUserCenter: no change, skip")
                    Toast.makeText(context, "请修改头像或昵称", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                userRepository.updateUserCenter(userId, avatar, name).getOrElse { e ->
                    Log.e(TAG, "updateUserCenter: fail", e)
                    if (e !is ApiException) {
                        Toast.makeText(context, e.message ?: "修改失败", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                Log.d(TAG, "updateUserCenter: success")
                ProfileSyncCenter.notifyProfileUpdated()
                _updateProfileSuccess.value = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                // ApiException 已在构造时经 ApiErrorNotifier 提示
            } catch (e: Exception) {
                Log.e(TAG, "updateUserCenter: unexpected", e)
                Toast.makeText(context, e.message ?: "修改失败", Toast.LENGTH_SHORT).show()
            } finally {
                _confirmLoading.value = false
            }
        }
    }

    /** 重置 updateProfileSuccess，在 Modal 关闭后调用 */
    fun resetUpdateProfileSuccess() {
        _updateProfileSuccess.value = false
    }

    /** 退出登录：清 token 与用户信息并回到登录页 */
    fun logout(context: Context) {
        TokenConfig.clearToken()
        UserConfig.clear()
        _teacherId.value = 0L
        NavigationRouteBridge.replaceRoot(AppRoute.Login)
    }

    companion object {
        private const val TAG = "ProfileViewModel"
    }
}
