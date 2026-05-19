package com.vortexa.ui.page.wallet.pay

import org.jetbrains.compose.resources.DrawableResource
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.icon_alipay
import vortexa.composeapp.generated.resources.icon_usdt
import vortexa.composeapp.generated.resources.icon_wechat

enum class PointRechargePayChannel(val label: String, val iconRes: DrawableResource) {
    WeChat("微信支付", Res.drawable.icon_wechat),
    Alipay("支付宝", Res.drawable.icon_alipay),
    Web3Wallet("Web3钱包支付", Res.drawable.icon_usdt)
}
