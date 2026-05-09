package com.vortexa.ui.page.wallet.detail

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.theme.BaseTheme

/**
 *  desc : TODO Fill the fucking desc
 *
 *
 *  @author LuXin
 *  @createTime 2026/2/27
 */
class DealDetailActivity: BaseActivity() {

    companion object {
        private const val EXTRA_DEAL_ID = "extra_deal_id"

        fun start(context: Context, dealId: String? = null) {
            context.startActivity(Intent(context, DealDetailActivity::class.java).apply {
                if (!dealId.isNullOrBlank()) putExtra(EXTRA_DEAL_ID, dealId)
            })
        }
    }
    // PRIVATE METHODS

    // PUBLIC METHODS
    @Composable
    override fun ContentPage() {
        BaseTheme(belowStatusBar = true, aboveNavigationBar = true) {
            DealDetailView(onBackClick = { finish() })
        }
    }
}