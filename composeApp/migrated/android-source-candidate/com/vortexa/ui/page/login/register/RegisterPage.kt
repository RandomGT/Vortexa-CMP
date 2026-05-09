package com.vortexa.ui.page.login.register

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vortexa.ui.component.LoadingButton
import com.vortexa.ui.page.login.AuthInput
import com.vortexa.ui.theme.BaseTheme
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.AUTH_PASSWORD_RULE_TIP
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res

/**
 * desc : Register Page with multiple steps
 *
 * @author LuXin
 * @createTime 2026/1/21
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RegisterPage() {
    val viewModel = viewModel { RegisterViewModel() }
    val state by viewModel.uiState.collectAsState()

    BaseTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Top Background Image (Reused from Login)
            Image(
                painter = painterResource(Res.drawable.bg_login),
                contentDescription = "Background",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(395.dp),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter
            )

            // Bottom Sheet Content
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(540.dp)
                    .background(
                        Color.White,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
            ) {
                // Animated Content for Steps
                AnimatedContent(
                    targetState = state.step,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { width -> width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> width } + fadeOut()
                        }
                    }
                ) { step ->
                    when (step) {
                        0 -> RegisterStep1(viewModel, state)
                        1 -> RegisterStep2(viewModel, state)
                    }
                }
            }
        }
    }
}

/**
 * Step 1: Phone and Verification Code
 */
@Composable
fun RegisterStep1(viewModel: RegisterViewModel, state: RegisterState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Phone Input
            AuthInput(
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                placeholder = "请输入手机号"
            )

            // Code Input with Get Code Button
            AuthInput(
                value = state.code,
                onValueChange = viewModel::onCodeChange,
                placeholder = "请输入验证码",
                trailingContent = {
                    val countdown = state.countdownSeconds
                    val isCountingDown = countdown > 0
                    Text(
                        text = if (isCountingDown) "${countdown}s" else "获取验证码",
                        style = FontRegular(
                            16,
                            if (isCountingDown) Colors.gray_6A7282 else Colors.blue_3266FF
                        ),
                        modifier = Modifier.clickable(enabled = !isCountingDown) {
                            viewModel.getVerificationCode()
                        }
                    )
                }
            )
        }

        LoadingButton(
            modifier = Modifier
                .padding(horizontal = 37.dp)
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Colors.black_101828),
            text = "下一步",
            isLoading = state.verifySmsLoading,
            onClick = viewModel::onNextStep,
            content = {
                Text(text = "下一步", style = FontMedium(16, Color.White))
            }
        )
    }
}

/**
 * Step 2: Username and Password
 */
@Composable
fun RegisterStep2(viewModel: RegisterViewModel, state: RegisterState) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(40.dp) // Adjusted spacing to fit content
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Username Input
            AuthInput(
                value = state.username,
                onValueChange = viewModel::onUsernameChange,
                placeholder = "请输入用户名"
            )

            // Password Input
            AuthInput(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = "请输入密码",
                isPassword = true
            )
            // Confirm Password Input
            AuthInput(
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                placeholder = "请再次输入密码确认",
                isPassword = true
            )
            Text(
                text = AUTH_PASSWORD_RULE_TIP,
                style = FontRegular(12, Colors.gray_6A7282),
                modifier = Modifier.width(315.dp)
            )


            state.error?.let { error ->
                Text(
                    text = error,
                    style = FontRegular(14, Color.Red),
                    modifier = Modifier.padding(horizontal = 37.dp)
                )
            }
        }

        // Register Button
        Box(
            modifier = Modifier
                .padding(horizontal = 37.dp)
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Colors.black_101828)
                .click { viewModel.onRegister(context) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (state.isLoading) "注册中..." else "注册",
                style = FontMedium(16, Color.White)
            )
        }
    }
}

@Preview
@Composable
fun RegisterPagePreview() {
    BaseTheme {
        RegisterPage()
    }
}
