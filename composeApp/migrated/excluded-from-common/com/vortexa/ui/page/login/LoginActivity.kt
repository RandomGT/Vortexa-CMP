package com.vortexa.ui.page.login

import android.content.Intent
import androidx.compose.runtime.Composable
import com.vortexa.ui.base.BaseActivity

class LoginActivity : BaseActivity() {

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    @Composable
    override fun ContentPage() {
        LoginScreen()
    }

    companion object {
        const val EXTRA_INLINE_AUTH = "inline_auth"
    }
}