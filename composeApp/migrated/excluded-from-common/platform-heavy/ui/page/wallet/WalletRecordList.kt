package com.vortexa.ui.page.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/** 单条积分记录数据 */
data class WalletRecord(
    val date: String,
    val amount: String,
    val action: String
)

/** 每页最多展示条数 */
const val WALLET_PAGE_SIZE = 10

/**
 * 积分记录列表：最多展示 [WALLET_PAGE_SIZE] 条，可上下滚动；样式与 [WalletRecordItem] 一致。
 *
 * @param records 当前页数据（建议不超过 [WALLET_PAGE_SIZE]）
 */
@Composable
fun WalletRecordList(
    records: List<WalletRecord>,
    modifier: Modifier = Modifier
) {
    val displayList = records.take(WALLET_PAGE_SIZE)
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White),
        userScrollEnabled = true
    ) {
        items(
            items = displayList,
            key = { "${it.date}-${it.amount}-${it.action}" }
        ) { record ->
            WalletRecordItem(
                date = record.date,
                amount = record.amount,
                action = record.action
            )
        }
    }
}

@Composable
private fun WalletRecordListPreview() {
    WalletRecordList(
        records = listOf(
            WalletRecord("2025-10-02", "+50", "充值"),
            WalletRecord("2025-10-01", "-20", "兑换")
        )
    )
}
