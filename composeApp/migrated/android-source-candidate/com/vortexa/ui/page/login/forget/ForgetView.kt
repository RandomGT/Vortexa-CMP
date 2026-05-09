package com.vortexa.ui.page.login.forget

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import android.app.Activity
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vortexa.ui.component.LoadingButton
import com.vortexa.ui.page.login.AuthInput
import com.vortexa.util.AUTH_PASSWORD_RULE_TIP
import com.vortexa.ui.theme.BaseTheme
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import vortexa.composeapp.generated.resources.Res

/**
 * 忘记密码页：三步（手机号+验证码 → 新密码+确认 → 找回成功），无用户名，底部「找回」校验两次密码一致后调重置接口，成功展示「去登录」返回登录页。
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ForgetView(viewModel: ForgetViewModel = viewModel { ForgetViewModel() }) {
    val state by viewModel.uiState.collectAsState()
    val resetSuccess by viewModel.resetSuccessToFinish.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(resetSuccess) {
        if (resetSuccess) {
            Toast.makeText(context, "密码重置成功", Toast.LENGTH_SHORT).show()
            (context as? Activity)?.finish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(Res.drawable.bg_login),
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxWidth()
                .height(395.dp),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter
        )
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
                    0 -> ForgetStep1(viewModel, state)
                    1 -> ForgetStep2(viewModel, state)
                    2 -> ForgetSuccess(viewModel)
                }
            }
        }
    }
}

/** Step1：手机号 + 验证码，无用户名；下一步进入 Step2 */
@Composable
fun ForgetStep1(viewModel: ForgetViewModel, state: ForgetState) {
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
            AuthInput(
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                placeholder = "请输入手机号"
            )
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

/** Step2：新密码 + 确认密码；点击「找回」校验一致后调重置接口 */
@Composable
fun ForgetStep2(viewModel: ForgetViewModel, state: ForgetState) {
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
            AuthInput(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = "请输入新密码",
                isPassword = true
            )
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
            state.error?.let { err ->
                Text(
                    text = err,
                    style = FontRegular(14, Color.Red),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(horizontal = 37.dp)
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Colors.black_101828)
                .clickable(onClick = viewModel::onReset),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (state.isLoading) "找回中..." else "找回",
                style = FontMedium(16, Color.White)
            )
        }
    }
}

/** Step3：找回成功，展示「去登录」按钮，点击结束当前 Activity 返回登录页 */
@Composable
fun ForgetSuccess(viewModel: ForgetViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Image(
            painter = painterResource(Res.drawable.icon_success),
            contentDescription = "Success",
            modifier = Modifier.size(80.dp)
        )
        Text(
            text = "找回成功",
            style = FontMedium(18, Colors.black_101828)
        )
        val context = LocalContext.current
        Box(
            modifier = Modifier
                .padding(top = 20.dp)
                .padding(horizontal = 37.dp)
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Colors.black_101828)
                .clickable(onClick = { (context as? Activity)?.finish() }),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "去登录", style = FontMedium(16, Color.White))
        }
    }
}

@Composable
@Preview
fun ForgetPreview() {
    BaseTheme {
        ForgetView()
    }
}
