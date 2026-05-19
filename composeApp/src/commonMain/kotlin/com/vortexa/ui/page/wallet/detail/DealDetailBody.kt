package com.vortexa.ui.page.wallet.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.ui.theme.FontSemiBold

@Composable
fun DealDetailBody(
    statusText: String,
    amountDisplay: String,
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = statusText, style = FontSemiBold(fontSize = 18, color = Colors.blue_277DFF))
            Text(text = amountDisplay, style = FontSemiBold(fontSize = 48, color = Colors.blue_277DFF))
        }
        Text(
            text = "交易明细",
            style = FontSemiBold(fontSize = 18, color = Colors.black_101828),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 6.dp)
        )
        rows.forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = label, style = FontRegular(fontSize = 15, color = Colors.gray_6A7282))
                Text(text = value, style = FontSemiBold(fontSize = 15, color = Colors.black_101828))
            }
        }
    }
}

