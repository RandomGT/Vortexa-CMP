package com.vortexa.ui.page.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.ui.theme.FontSemiBold
import com.vortexa.util.extension.click
import org.jetbrains.compose.resources.painterResource
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.ic_arrow_right_gray

@Composable
fun WalletRecordItem(
    date: String,
    amount: String,
    action: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .click(onClickListener = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date,
            style = FontRegular(fontSize = 15, color = Colors.gray_6A7282)
        )
        Text(
            text = amount,
            style = FontSemiBold(fontSize = 15, color = Colors.black_101828)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = action,
                style = FontSemiBold(fontSize = 15, color = Colors.black_101828)
            )
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_right_gray),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Colors.black_101828
            )
        }
    }
}

