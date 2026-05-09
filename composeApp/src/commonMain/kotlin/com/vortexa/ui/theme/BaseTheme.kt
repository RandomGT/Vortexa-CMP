package com.vortexa.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

fun Modifier.belowStatusBar(): Modifier = statusBarsPadding()

@Composable
fun BaseTheme(
    statusBarTextDark: Boolean = true,
    navigationBarTextDark: Boolean = true,
    belowStatusBar: Boolean = true,
    aboveNavigationBar: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme {
        var modifier: Modifier = Modifier
        if (belowStatusBar) modifier = modifier.statusBarsPadding()
        if (aboveNavigationBar) modifier = modifier.navigationBarsPadding()
        Box(modifier) { content() }
    }
}
