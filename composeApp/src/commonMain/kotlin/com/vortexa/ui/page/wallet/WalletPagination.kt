package com.vortexa.ui.page.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.util.extension.click

@Composable
fun WalletPagination(
    currentPage: Int,
    totalPages: Int,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WalletPageButton(
                text = "上一页",
                modifier = Modifier.weight(0.5f),
                onClick = onPrevClick
            )
            Text(
                modifier = Modifier.weight(0.5f),
                textAlign = TextAlign.Center,
                text = "$currentPage/$totalPages",
                style = FontMedium(fontSize = 16, color = Colors.black_101828)
            )
            WalletPageButton(
                text = "下一页",
                modifier = Modifier.weight(0.5f),
                onClick = onNextClick
            )
        }
    }
}

@Composable
private fun WalletPageButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Colors.gray_EEF0F1, RoundedCornerShape(30.dp))
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .click(onClickListener = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = FontMedium(fontSize = 16, color = Colors.black_101828)
        )
    }
}

