package com.vortexa.ui.page.wallet

/** 钱包页 Tab 文案：全部、获得、支出。 */
val WalletTabs = listOf("全部", "获得", "支出")

/** 每页最多展示条数。 */
const val WALLET_PAGE_SIZE = 10

enum class WalletRecordType {
    Income,
    Expense
}

data class WalletRecord(
    val id: String,
    val date: String,
    val amount: String,
    val action: String,
    val type: WalletRecordType
)

data class WalletUiState(
    val balance: Int = 0,
    val records: List<WalletRecord> = defaultWalletRecords(),
    val selectedTabIndex: Int = 0,
    val pageByTab: Map<Int, Int> = WalletTabs.indices.associateWith { 1 }
) {
    fun recordsForTab(tabIndex: Int): List<WalletRecord> = when (tabIndex) {
        1 -> records.filter { it.type == WalletRecordType.Income }
        2 -> records.filter { it.type == WalletRecordType.Expense }
        else -> records
    }

    fun currentPage(tabIndex: Int): Int = pageByTab[tabIndex] ?: 1

    fun totalPages(tabIndex: Int): Int {
        val count = recordsForTab(tabIndex).size
        return ((count + WALLET_PAGE_SIZE - 1) / WALLET_PAGE_SIZE).coerceAtLeast(1)
    }
}

fun WalletRecord.toDealDetailState(): DealDetailState = DealDetailState(
    dealId = id,
    statusText = "支付成功",
    amountDisplay = amount,
    detailRows = listOf(
        "当前状态" to "完成",
        "订单金额" to amount.removePrefix("+").removePrefix("-"),
        "优惠折扣" to "-20.00",
        "支付时间" to "$date 16:00",
        "支付方式" to action,
        "商品说明" to "积分充值",
        "订单号" to id
    )
)

fun defaultWalletRecords(): List<WalletRecord> = listOf(
    WalletRecord("16456549649612121", "2025-10-02", "+50", "充值", WalletRecordType.Income),
    WalletRecord("16456549649612120", "2025-10-01", "-20", "兑换", WalletRecordType.Expense),
    WalletRecord("16456549649612119", "2025-09-30", "+100", "充值", WalletRecordType.Income)
)

