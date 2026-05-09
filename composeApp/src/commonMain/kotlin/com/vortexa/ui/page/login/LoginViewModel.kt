package com.vortexa.ui.page.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.config.TokenConfig
import com.vortexa.config.UserConfig
import com.vortexa.ui.page.login.register.RegisterActivity
import com.vortexa.util.extension.routeToPage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.vortexa.model.AuthRequest
import com.vortexa.repository.AccountRepository
import com.vortexa.router.PostAuthNavigator
import com.vortexa.ui.page.login.forget.ForgetActivity
import com.vortexa.util.PHONE_ELEVEN_DIGIT_TOAST
import com.vortexa.util.ToastUtil
import com.vortexa.util.isElevenDigitMobile

/**
 * desc : Login ViewModel to handle UI state and business logic
 *
 * @author LuXin
 * @createTime 2026/1/21
 */
class LoginViewModel(
    private val accountRepository: AccountRepository = AccountRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    /**
     * Update username input
     */
    fun onUsernameChange(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    /**
     * Update password input
     */
    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    /**
     * Toggle password visibility
     */
    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    /**
     * Handle login action
     */
    fun login(
        context: Context,
        inlineAuthAfterLogin: Boolean = false,
        onSuccess: (() -> Unit)? = null
    ) {
        val currentState = _uiState.value
        val userRaw = currentState.username
        val passRaw = currentState.password
        when {
            userRaw.isBlank() && passRaw.isBlank() -> {
                ToastUtil.show(context, "请输入账号和密码")
                return
            }
            userRaw.isBlank() -> {
                ToastUtil.show(context, "请输入账号")
                return
            }
            passRaw.isBlank() -> {
                ToastUtil.show(context, "请输入密码")
                return
            }
        }
        val userName = userRaw.trim()
        if (!isElevenDigitMobile(userName)) {
            ToastUtil.show(context, PHONE_ELEVEN_DIGIT_TOAST)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val request = AuthRequest(
                authType = "LOGIN_PASSWORD",
                userName = userName,
                password = currentState.password,
                verifyCode = currentState.verifyCode // Assuming verifyCode is added to state
            )

            accountRepository.auth(request)
                .onSuccess { response ->
                    Log.i("Login", "login: success!!! ")
                    _uiState.update { it.copy(isLoading = false) }
                    // Save token and user info
                    UserConfig.saveUserInfo(response.userInfoLogin)
                    TokenConfig.updateToken(response.token)
                    onSuccess?.invoke() ?: goHome(context, inlineAuthAfterLogin)
                }
                .onFailure { e ->
                    Log.e("Login", "login: fail!!! :$e")
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun goHome(context: Context, inlineAuthAfterLogin: Boolean = false) {
        PostAuthNavigator.navigateAfterLogin(context, inlineAuth = inlineAuthAfterLogin)
    }

    fun routeToRegister(context: Context) {
        context.routeToPage(RegisterActivity::class)
    }

    fun onForget(context: Context) {
        context.routeToPage(ForgetActivity::class)
    }
}

data class LoginState(
    val username: String = "",
    val password: String = "",
    val verifyCode: String = "", // Added verifyCode
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
