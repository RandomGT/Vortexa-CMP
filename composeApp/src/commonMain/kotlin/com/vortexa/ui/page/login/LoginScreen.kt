package com.vortexa.ui.page.login

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.BaseTheme
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.ui.component.LoadingButton
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.bg_login

/**
 * desc : Login Screen Implementation
 *
 * @author LuXin
 * @createTime 2026/1/21
 */
@Composable
fun LoginScreen(
    onRegisterClick: (() -> Unit)? = null,
    onForgetClick: (() -> Unit)? = null,
    onLoginSuccess: (() -> Unit)? = null
) {
    val viewModel = vortexaViewModel { LoginViewModel() }
    val state by viewModel.uiState.collectAsState()
    BaseTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Background Image
           Image(
                painter = painterResource(Res.drawable.bg_login),
                contentDescription = "Background",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(395.dp), // Height from Figma
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter
            )

            // Bottom Sheet Content
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(540.dp) // Height from Figma
                    .background(
                        Color.White,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(40.dp)
                ) {
                    // Inputs Column
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Username Input
                        AuthInput(
                            value = state.username,
                            onValueChange = viewModel::onUsernameChange,
                            placeholder = "请输入账号"
                        )

                        // Password Input
                        AuthInput(
                            value = state.password,
                            onValueChange = viewModel::onPasswordChange,
                            placeholder = "请输入密码",
                            isPassword = true,
                            isPasswordVisible = state.isPasswordVisible,
                            onTogglePasswordVisibility = viewModel::togglePasswordVisibility
                        )
                    }

                    // Forgot Password
                    Text(
                        text = "忘记密码",
                        style = FontRegular(14, Colors.gray_6A7282),
                        modifier = Modifier.click {
                            onForgetClick?.invoke() ?: viewModel.onForget(Context())
                        }
                    )

                    // Login Button
                    LoadingButton(
                        modifier = Modifier
                            .padding(horizontal = 37.dp) // 390 width - 315 input width = 75 / 2 = 37.5. 
                            // Actually input width in Figma is 315. Screen width 390. (390-315)/2 = 37.5.
                            // Button width in Figma is "w-full" inside a container? 
                            // The button is width 315px as well if it matches input. 
                            // Let's use fillMaxWidth with padding.
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Colors.black_101828),
                        text = "登录",
                        isLoading = state.isLoading,
                        onClick = {
                            val context = Context()
                            val inline = (context as? LoginActivity)?.intent
                                ?.getBooleanExtra(LoginActivity.EXTRA_INLINE_AUTH, false) == true
                            viewModel.login(context, inline, onLoginSuccess)
                        }
                    )

                    // Register Link
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "没有账号？",
                            style = FontRegular(16, Colors.gray_6A7282)
                        )
                        Text(
                            text = "去注册",
                            style = FontRegular(16, Colors.blue_3266FF),
                            modifier = Modifier.click {
                                onRegisterClick?.invoke() ?: viewModel.routeToRegister(Context())
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun LoginScreenPreview() {
    LoginScreen()
}
