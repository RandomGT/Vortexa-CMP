package com.vortexa.ui.page.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vortexa.ui.page.wallet.pay.PointRechargeActivity
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.ui.theme.FontSemiBold
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res

/**
 * 钱包页头部（Figma 336-21449）：左侧返回箭头 + 居中标题「钱包」，深色背景下白字/白图标。
 *
 * @param onBackClick 点击返回箭头回调
 */
@Composable
fun WalletHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: WalletViewModel = viewModel()
    val balance by viewModel.balance.collectAsState()
    Box(modifier = Modifier.clip(RoundedCornerShape(bottomEnd = 50.dp))
    ) {
        Image(painterResource(Res.drawable.bg_profile), contentDescription = "",
            contentScale = ContentScale.Crop,)
        Column {
            Row(
                modifier = modifier
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
                onRechargeClick = { PointRechargeActivity.start(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 18.dp)
            )
        }
    }
}


/**
 * 积分展示区（Figma 336-21453）：大号积分数字 + 右侧「充值」胶囊按钮，下方「积分」副标题。
 *
 * @param balance 积分数值
 * @param onRechargeClick 点击充值
 */
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


@Composable
@Preview(backgroundColor = 0xFF101828)
private fun WalletHeaderPreview() {
    WalletHeader(onBackClick = {})
}
