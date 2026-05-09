package com.vortexa.ui.page.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import com.vortexa.router.AppSchemeContract
import com.vortexa.router.AppSchemeRouter
import com.vortexa.ui.base.BaseActivity

/**
 *  desc :
 *
 *
 *  @author LuXin
 *  @createTime 2026/2/4
 */
class HomeActivity : BaseActivity() {

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            applyHomeTabFromIntentExtra(intent)
            val schemeIntent = intent
            mainHandler.post { AppSchemeRouter.consumeViewIntentIfScheme(this, schemeIntent) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        applyHomeTabFromIntentExtra(intent)
        val schemeIntent = intent
        mainHandler.post { AppSchemeRouter.consumeViewIntentIfScheme(this, schemeIntent) }
    }

    private fun applyHomeTabFromIntentExtra(intent: Intent?) {
        val tab = intent?.getIntExtra(AppSchemeContract.EXTRA_HOME_TAB, -1) ?: -1
        if (tab in 0..4) {
            if (HomeGuestTabLogin.openGuestLoginInsteadOfTab(this, tab)) {
                intent?.removeExtra(AppSchemeContract.EXTRA_HOME_TAB)
                return
            }
            ViewModelProvider(this)[HomeViewModel::class.java].onTabClick(tab)
            intent?.removeExtra(AppSchemeContract.EXTRA_HOME_TAB)
        }
    }

    @Composable
    override fun ContentPage() {
        HomePage()
    }
}
