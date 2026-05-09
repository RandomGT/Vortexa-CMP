package com.vortexa.ui.page.wallet.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

/**
 * 交易详情页主视图（Figma 336-14697）：白底，由上至下为导航栏、支付状态与金额、交易明细列表、底部三按钮。
 *
 * @param onBackClick 点击返回
 * @param statusText 状态文案，如「支付成功」
 * @param amountDisplay 金额展示，如「$100」
 * @param detailRows 交易明细行（label to value）
 * @param onRecordClick 往来记录
 * @param onQuestionClick 订单疑问
 * @param onContactClick 联系商家
 */
@Composable
fun DealDetailView(
    onBackClick: () -> Unit = {},
    statusText: String = "支付成功",
    amountDisplay: String = "\$100",
    detailRows: List<Pair<String, String>> = defaultDetailRows(),
    onRecordClick: () -> Unit = {},
    onQuestionClick: () -> Unit = {},
    onContactClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        DealDetailToolbar(onBackClick = onBackClick)
        DealDetailBody(
            statusText = statusText,
            amountDisplay = amountDisplay,
            rows = detailRows,
            modifier = Modifier.weight(1f)
        )
        DealDetailBottomBar(
            onRecordClick = onRecordClick,
            onQuestionClick = onQuestionClick,
            onContactClick = onContactClick
        )
    }
}

/** 默认明细行数据（与 Figma 示例一致） */
private fun defaultDetailRows(): List<Pair<String, String>> = listOf(
    "当前状态" to "完成",
    "订单金额" to "120.00",
    "优惠折扣" to "-20.00",
    "支付时间" to "2025-10-08 16:00",
    "支付方式" to "支付宝",
    "商品说明" to "积分充值",
    "订单号" to "16456549649612121"
)

@Composable
@Preview
fun DealDetailPreview() {
    DealDetailView(onBackClick = {})
}
