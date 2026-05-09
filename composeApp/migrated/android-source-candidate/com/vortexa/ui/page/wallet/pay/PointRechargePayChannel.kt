package com.vortexa.ui.page.wallet.pay


import vortexa.composeapp.generated.resources.Res
/**
 * 积分充值可选支付方式。
 *
 * @param iconRes [R.mipmap] 渠道图标
 */
enum class PointRechargePayChannel(val label: String, val iconRes: Int) {
    WeChat("微信支付", Res.drawable.icon_wechat),
    Alipay("支付宝", Res.drawable.icon_alipay),
    Web3Wallet("Web3钱包支付", Res.drawable.icon_usdt)
}
