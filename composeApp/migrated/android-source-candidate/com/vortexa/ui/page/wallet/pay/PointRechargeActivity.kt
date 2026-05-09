package com.vortexa.ui.page.wallet.pay

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.theme.BaseTheme

/**
 * 积分充值：展示余额、输入充值数量、选择支付方式、协议勾选与「下一步」。
 */
class PointRechargeActivity : BaseActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, PointRechargeActivity::class.java))
        }
    }

    @Composable
    override fun ContentPage() {
        BaseTheme(belowStatusBar = true, aboveNavigationBar = true) {
            PointRechargeView(onBackClick = { finish() })
        }
    }
}
