package com.vortexa.navigation

import androidx.compose.runtime.Composable

@Composable
internal expect fun WideBackGestureLayer(
    enabled: Boolean,
    content: @Composable () -> Unit,
)
