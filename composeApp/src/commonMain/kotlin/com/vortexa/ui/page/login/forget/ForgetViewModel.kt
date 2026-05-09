package com.vortexa.ui.page.login.forget

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.model.ResetPasswordRequest
import com.vortexa.repository.AccountRepository
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

/** 验证码场景：忘记密码 */
/**
 * 忘记密码页 ViewModel：管理步骤、手机号/验证码/新密码、找回请求与成功态
 */
class ForgetViewModel(
    private val accountRepository: AccountRepository = AccountRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgetState())
    val uiState: StateFlow<ForgetState> = _uiState.asStateFlow()

    /** 一次性事件：重置成功，UI 收到后弹 Toast 并 finish */
    private val _resetSuccessToFinish = MutableStateFlow(false)
    val resetSuccessToFinish: StateFlow<Boolean> = _resetSuccessToFinish.asStateFlow()

    private var countdownJob: Job? = null

    fun onPhoneChange(phone: String) {
        _uiState.update { it.copy(phone = phone) }
    }

    fun onCodeChange(code: String) {
        _uiState.update { it.copy(code = code) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, error = null) }
    }

    /**
     * 获取短信验证码，成功后开启 60 秒倒计时
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
                .onSuccess { startCountdown() }
                .onFailure { e ->
                    Log.e(TAG, "getVerificationCode: 请求失败", e)
                }
        }
    }

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

    /** Step1 点击下一步：先调短信验证码校验接口，成功后再进入 Step2 */
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
                    _uiState.update {
                        it.copy(step = 1, error = null, verifySmsLoading = false)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(verifySmsLoading = false) }
                    Log.e(TAG, "onNextStep: 验证码校验失败", e)
                    ToastUtil.show(e.message ?: "验证失败")
                }
        }
    }

    /**
     * 点击找回：校验两次密码一致后调用重置接口，成功则进入成功页
     */
    fun onReset() {
        val currentState = _uiState.value
        val phone = currentState.phone.trim()
        val code = currentState.code.trim()
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
            currentState.password.isBlank() -> {
                ToastUtil.show("请输入新密码")
                return
            }
            currentState.confirmPassword.isBlank() -> {
                ToastUtil.show("请再次输入密码确认")
                return
            }
        }
        if (!AUTH_PASSWORD_REGEX.matches(currentState.password)) {
            _uiState.update { it.copy(error = AUTH_PASSWORD_RULE_TIP) }
            Log.w(TAG, "onReset: 新密码不符合复杂度要求")
            return
        }
        if (currentState.password != currentState.confirmPassword) {
            _uiState.update { it.copy(error = "两次密码不一致") }
            Log.w(TAG, "onReset: 两次密码不一致")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val request = ResetPasswordRequest(
                phone = phone,
                smsCode = code,
                newPassword = currentState.password
            )
            accountRepository.resetPassword(request)
                .onSuccess {
                    Log.i(TAG, "onReset: 密码重置成功")
                    _uiState.update { it.copy(step = 2, isLoading = false) }
                    _resetSuccessToFinish.value = true
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message)
                    }
                    Log.e(TAG, "onReset: 重置失败", e)
                }
        }
    }

    companion object {
        private const val TAG = "ForgetViewModel"
    }
}

/** 0: 手机号+验证码  1: 新密码+确认  2: 成功 */
data class ForgetState(
    val step: Int = 0,
    val phone: String = "",
    val code: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    /** Step1「下一步」调用 /sms/verify 时的加载态 */
    val verifySmsLoading: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val countdownSeconds: Int = 0
)
