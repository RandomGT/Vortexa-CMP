package com.vortexa.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
inline fun <reified VM : ViewModel> vortexaViewModel(
    key: String? = null,
    noinline initializer: () -> VM,
): VM = viewModel(
    key = key,
    initializer = { initializer() },
)
