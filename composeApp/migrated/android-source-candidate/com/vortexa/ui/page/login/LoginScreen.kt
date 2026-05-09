package com.vortexa.ui.page.login

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vortexa.ui.theme.BaseTheme
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res

/**
 * desc : Login Screen Implementation
 *
 * @author LuXin
 * @createTime 2026/1/21
 */
@Composable
fun LoginScreen() {
    val viewModel = viewModel { LoginViewModel() }
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
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
                            viewModel.onForget(context)
                        }
                    )

                    // Login Button
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 37.dp) // 390 width - 315 input width = 75 / 2 = 37.5. 
                            // Actually input width in Figma is 315. Screen width 390. (390-315)/2 = 37.5.
                            // Button width in Figma is "w-full" inside a container? 
                            // The button is width 315px as well if it matches input. 
                            // Let's use fillMaxWidth with padding.
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Colors.black_101828)
                            .click {
                                val inline = (context as? LoginActivity)?.intent
                                    ?.getBooleanExtra(LoginActivity.EXTRA_INLINE_AUTH, false) == true
                                viewModel.login(context, inline)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "登录",
                            style = FontMedium(16, Color.White)
                        )
                    }

                    // Register Link
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "没有账号？",
                            style = FontRegular(16, Colors.gray_6A7282)
                        )
                        val context = LocalContext.current
                        Text(
                            text = "去注册",
                            style = FontRegular(16, Colors.blue_3266FF),
                            modifier = Modifier.click {
                                viewModel.routeToRegister(context)
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
@Preview(device = "id:pixel_9a")
fun LoginScreenPreview() {
    LoginScreen()
}