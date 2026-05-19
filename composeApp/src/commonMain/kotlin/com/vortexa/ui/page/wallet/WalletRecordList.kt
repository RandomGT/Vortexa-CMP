package com.vortexa.ui.page.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun WalletRecordList(
    records: List<WalletRecord>,
    onRecordClick: (WalletRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White),
        userScrollEnabled = true
    ) {
        items(
            items = records.take(WALLET_PAGE_SIZE),
            key = { it.id }
        ) { record ->
            WalletRecordItem(
                date = record.date,
                amount = record.amount,
                action = record.action,
                onClick = { onRecordClick(record) }
            )
        }
    }
}

