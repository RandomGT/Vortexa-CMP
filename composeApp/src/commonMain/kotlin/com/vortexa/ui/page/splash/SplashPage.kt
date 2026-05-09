package com.vortexa.ui.page.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.BaseTheme
import com.vortexa.ui.theme.FontTitle
import kotlinx.coroutines.delay
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.splash_bg

/**
 * 闪屏页 UI。
 * @param onSplashFinish 闪屏展示结束后的回调（由调用方跳转首页等）。
 */
@Composable
fun SplashPage(onSplashFinish: () -> Unit = {}) {

    BaseTheme(belowStatusBar = false, aboveNavigationBar = false) {
        Column(Modifier.fillMaxSize()
            .background(Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally){
            Image(
                modifier = Modifier
                    .fillMaxSize(),
                painter = painterResource(Res.drawable.splash_bg),
                contentDescription = "logo",
                contentScale = ContentScale.Crop
            )
        }
    }

    LaunchedEffect(Unit) {
        delay(2000)
        onSplashFinish()
    }
}

/**
 * 仅用于预览，不包含跳转逻辑。
 */
@Composable
fun SplashPagePreview() {
    SplashPage()
}