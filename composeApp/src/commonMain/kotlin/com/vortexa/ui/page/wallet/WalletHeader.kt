package com.vortexa.ui.page.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.ui.theme.FontSemiBold
import com.vortexa.util.extension.click
import org.jetbrains.compose.resources.painterResource
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.bg_profile
import vortexa.composeapp.generated.resources.ic_arrow_left

@Composable
fun WalletHeader(
    balance: Int,
    onBackClick: () -> Unit,
    onRechargeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(214.dp)
            .clip(RoundedCornerShape(bottomEnd = 50.dp))
    ) {
        Image(
            painter = painterResource(Res.drawable.bg_profile),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(top = 40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_left),
                    contentDescription = "返回",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .click(onClickListener = onBackClick)
                )
            }

            Spacer(modifier = Modifier.height(17.dp))
            WalletBalanceSection(
                balance = balance,
                onRechargeClick = onRechargeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 18.dp)
            )
        }
    }
}

@Composable
private fun WalletBalanceSection(
    balance: Int,
    onRechargeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = balance.toString(),
                style = FontSemiBold(fontSize = 48, color = Color.White),
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Box(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(30.dp))
                    .padding(horizontal = 20.dp, vertical = 5.dp)
                    .click(onClickListener = onRechargeClick)
            ) {
                Text(
                    text = "充值",
                    style = FontMedium(fontSize = 14, color = Colors.black_101828)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "积分",
            style = FontRegular(fontSize = 16, color = Color.White)
        )
    }
}

