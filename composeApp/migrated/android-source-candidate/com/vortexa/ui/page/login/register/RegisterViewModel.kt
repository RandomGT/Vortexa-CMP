package com.vortexa.ui.page.login.register

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.config.TokenConfig
import com.vortexa.config.UserConfig
import com.vortexa.model.AuthRequest
import com.vortexa.repository.AccountRepository
import com.vortexa.router.PostAuthNavigator
import com.vortexa.util.AUTH_PASSWORD_REGEX
import com.vortexa.util.AUTH_PASSWORD_RULE_TIP
import com.vortexa.util.PHONE_ELEVEN_DIGIT_TOAST
import com.vortexa.util.ToastUtil
import com.vortexa.util.isElevenDigitMobile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 验证码倒计时秒数 */
private const val COUNTDOWN_SECONDS = 60

/** 验证码场景：注册 */
/**
 * desc : Register ViewModel to manage registration steps and state
 *
 * @author LuXin
 * @createTime 2026/1/21
 */
class RegisterViewModel(
    private val accountRepository: AccountRepository = AccountRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterState())
    val uiState: StateFlow<RegisterState> = _uiState.asStateFlow()

    /** 倒计时任务，用于取消 */
    private var countdownJob: Job? = null

    /**
     * Update phone number input
     */
    fun onPhoneChange(phone: String) {
        _uiState.update { it.copy(phone = phone) }
    }

    /**
     * Update verification code input
     */
    fun onCodeChange(code: String) {
        _uiState.update { it.copy(code = code) }
    }

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
        _uiState.update { it.copy(password = password, error = null) }
    }

    /**
     * Update confirm password input
     */
    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, error = null) }
    }

    /**
     * 获取短信验证码，请求成功后开启 60 秒倒计时
     */
    fun getVerificationCode() {
        val phone = _uiState.value.phone.trim()
        if (phone.isBlank()) {
            ToastUtil.show("请输入手机号")
            return
        }
        if (!isElevenDigitMobile(phone)) {
            ToastUtil.show(PHONE_ELEVEN_DIGIT_TOAST)
            return
        }
        if (_uiState.value.countdownSeconds > 0) {
            Log.d(TAG, "getVerificationCode: 倒计时中，忽略重复点击")
            return
        }
        viewModelScope.launch {
            accountRepository.getSmsCode(phone)
                .onSuccess {
                    startCountdown()
                }
                .onFailure { e ->
                    Log.e(TAG, "getVerificationCode: 请求失败", e)
                    // TODO: 根据项目方式展示错误（如 Toast）
                }
        }
    }

    /**
     * 开启 60 秒倒计时，期间禁止重复点击
     */
    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (seconds in COUNTDOWN_SECONDS downTo 1) {
                _uiState.update { it.copy(countdownSeconds = seconds) }
                delay(1000)
            }
            _uiState.update { it.copy(countdownSeconds = 0) }
        }
    }

    companion object {
        private const val TAG = "RegisterViewModel"
    }

    /**
     * Step1 下一步：校验短信验证码成功后进入 Step2
     */
    fun onNextStep() {
        val s = _uiState.value
        val phone = s.phone.trim()
        val code = s.code.trim()
        when {
            phone.isBlank() -> {
                ToastUtil.show("请输入手机号")
                return
            }
            !isElevenDigitMobile(phone) -> {
                ToastUtil.show(PHONE_ELEVEN_DIGIT_TOAST)
                return
            }
            code.isBlank() -> {
                ToastUtil.show("请输入验证码")
                return
            }
        }
        if (s.verifySmsLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(verifySmsLoading = true) }
            accountRepository.verifySmsCode(phone, code)
                .onSuccess {
                    _uiState.update { it.copy(step = 1, verifySmsLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(verifySmsLoading = false) }
                    Log.e(TAG, "onNextStep: 验证码校验失败", e)
                    ToastUtil.show(e.message ?: "验证失败")
                }
        }
    }

    /**
     * 提交注册；与登录共用 auth 接口，成功即写入 Token/用户信息并进入首页
     */
    fun onRegister(context: Context) {
        val currentState = _uiState.value
        val phone = currentState.phone.trim()
        val code = currentState.code.trim()
        val nickname = currentState.username.trim()
        when {
            phone.isBlank() -> {
                ToastUtil.show("请输入手机号")
                return
            }
            !isElevenDigitMobile(phone) -> {
                ToastUtil.show(PHONE_ELEVEN_DIGIT_TOAST)
                return
            }
            code.isBlank() -> {
                ToastUtil.show("请输入验证码")
                return
            }
            nickname.isBlank() -> {
                ToastUtil.show("请输入用户名")
                return
            }
            currentState.password.isBlank() -> {
                ToastUtil.show("请输入密码")
                return
            }
            currentState.confirmPassword.isBlank() -> {
                ToastUtil.show("请再次输入密码确认")
                return
            }
        }
        val passwordError = validatePassword(currentState.password)
        if (passwordError != null) {
            Log.w(TAG, "onRegister: 密码格式不符合要求")
            _uiState.update { it.copy(error = passwordError) }
            return
        }
        if (currentState.password != currentState.confirmPassword) {
            Log.w(TAG, "onRegister: 两次密码不一致")
            _uiState.update { it.copy(error = "两次密码不一致") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val request = AuthRequest(
                authType = "REGISTER",
                phone = phone,
                smsCode = code,
                password = currentState.password,
                nickname = nickname
            )

            accountRepository.auth(request)
                .onSuccess { response ->
                    Log.i(TAG, "onRegister: 注册成功, userId=${response.userInfoLogin.id}")
                    _uiState.update { it.copy(isLoading = false) }
                    UserConfig.saveUserInfo(response.userInfoLogin)
                    TokenConfig.updateToken(response.token)
                    goHome(context)
                }
                .onFailure { e ->
                    Log.e(TAG, "onRegister: 注册失败", e)
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    /**
     * 校验密码是否满足服务端复杂度规则。
     * @param password 用户输入的密码
     * @return 不满足规则时返回错误文案，满足时返回 null
     */
    private fun validatePassword(password: String): String? {
        return if (AUTH_PASSWORD_REGEX.matches(password)) {
            null
        } else {
            AUTH_PASSWORD_RULE_TIP
        }
    }
    
    private fun goHome(context: Context) {
        PostAuthNavigator.navigateAfterRegister(context)
    }
}

data class RegisterState(
    val step: Int = 0, // 0: 手机号+验证码, 1: 用户名+密码
    val phone: String = "",
    val code: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    /** Step1「下一步」调用 /sms/verify 时的加载态 */
    val verifySmsLoading: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    /** 验证码倒计时秒数，>0 时表示倒计时中，禁止重复点击 */
    val countdownSeconds: Int = 0
)
