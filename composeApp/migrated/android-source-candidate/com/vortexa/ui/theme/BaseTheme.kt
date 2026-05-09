package com.vortexa.ui.theme

import android.app.Activity
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 应用根主题，支持状态栏样式与留白配置。
 *
 * @param content 主题下的可组合内容
 * @param statusBarTextDark true=状态栏图标深色（浅色背景），false=状态栏图标浅色（深色背景）
 * @param belowStatusBar true=整体内容在状态栏下方留白，false=内容边到边，子 View 可自行使用 Modifier.belowStatusBar()
 * @param aboveNavigationBar true=整体内容在导航栏上方留白，false=内容边到边，子 View 可自行使用 Modifier.aboveNavigationBar()
 */
@Composable
fun BaseTheme(
    statusBarTextDark: Boolean = true,
    navigationBarTextDark: Boolean = true,
    belowStatusBar: Boolean = false,
    aboveNavigationBar: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    // 设置状态栏图标颜色（深色/浅色），需用 decorView 并在每次重组时应用
    SideEffect {
        val activity = view.context as? Activity
        activity?.let {
            WindowCompat.getInsetsController(it.window, it.window.decorView).apply {
                isAppearanceLightStatusBars = statusBarTextDark
                isAppearanceLightNavigationBars = navigationBarTextDark
            }
        }
    }
    MaterialTheme(
        content = {
            var modifier: Modifier = Modifier
            if (belowStatusBar) {
                modifier = modifier.windowInsetsPadding(WindowInsets.statusBars)
            }
            if (aboveNavigationBar) {
                modifier = modifier.windowInsetsPadding(WindowInsets.navigationBars)
            }
            Box(modifier = modifier) {
                content()
            }
        }
    )
}

/**
 * 为当前 View 添加状态栏高度留白，使内容不被状态栏遮挡。
 * 可在 BaseTheme(belowStatusBar = false) 时由子 View 按需使用。
 */
fun Modifier.belowStatusBar(): Modifier = this.statusBarsPadding()

/**
 * 为当前 View 添加导航栏高度留白，使内容不被导航栏遮挡。
 * 可在 BaseTheme(aboveNavigationBar = false) 时由子 View 按需使用。
 */
fun Modifier.aboveNavigationBar(): Modifier = this.navigationBarsPadding()
