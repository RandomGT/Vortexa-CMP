package com.vortexa.ui.base

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat

/**
 * 所有 Activity 的基类，统一使用沉浸式主题（透明状态栏/导航栏，内容边到边绘制）。
 */
abstract class BaseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Vortexa_Immersive)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ContentPage()
        }
    }

    @Composable
    abstract fun ContentPage()
}