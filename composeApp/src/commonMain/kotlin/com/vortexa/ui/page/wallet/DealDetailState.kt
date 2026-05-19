package com.vortexa.ui.page.wallet

data class DealDetailState(
    val dealId: String? = null,
    val statusText: String = "支付成功",
    val amountDisplay: String = "$100",
    val detailRows: List<Pair<String, String>> = defaultDetailRows()
)

fun defaultDetailRows(): List<Pair<String, String>> = listOf(
    "当前状态" to "完成",
    "订单金额" to "120.00",
    "优惠折扣" to "-20.00",
    "支付时间" to "2025-10-08 16:00",
    "支付方式" to "支付宝",
    "商品说明" to "积分充值",
    "订单号" to "16456549649612121"
)

