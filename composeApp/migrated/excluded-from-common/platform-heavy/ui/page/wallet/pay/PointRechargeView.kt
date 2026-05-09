package com.vortexa.ui.page.wallet.pay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vortexa.ui.component.LoadingButton
import com.vortexa.ui.page.wallet.WalletViewModel
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular

/**
 * 积分充值页：余额、充值数量输入、说明、支付方式、底部协议与「下一步」。
 */
@Composable
fun PointRechargeView(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    walletViewModel: WalletViewModel = viewModel()
) {
    val balance by walletViewModel.balance.collectAsState()
    var amountText by remember { mutableStateOf("") }
    var selectedChannel by remember { mutableStateOf(PointRechargePayChannel.WeChat) }
    var agreed by remember { mutableStateOf(false) }
    var nextLoading by remember { mutableStateOf(false) }

    val points = amountText.toIntOrNull() ?: 0
    val canNext = points > 0 && agreed

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        PointRechargeTopBar(onBackClick = onBackClick)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "当前积分余额",
                style = FontMedium(fontSize = 14, color = Colors.gray_6A7282)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$balance",
                style = FontMedium(fontSize = 28, color = Colors.black_101828)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "充值积分数量",
                style = FontMedium(fontSize = 14, color = Colors.black_101828)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { new ->
                    if (new.length <= 9 && (new.isEmpty() || new.all { it.isDigit() })) {
                        amountText = new
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "请输入积分数量",
                        style = FontRegular(14, Colors.gray_B1B8C6)
                    )
                },
                textStyle = FontRegular(16, Colors.black_101828),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Colors.blue_3266FF,
                    unfocusedBorderColor = Colors.gray_E5E8EB,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "10积分/元",
                style = FontRegular(fontSize = 13, color = Colors.gray_6A7282)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "选择充值方式",
                style = FontMedium(fontSize = 14, color = Colors.black_101828)
            )
            Spacer(modifier = Modifier.height(12.dp))
            PointRechargePayChannelList(
                selected = selectedChannel,
                onSelect = { selectedChannel = it }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(bottom = 12.dp)
        ) {
            PointRechargeAgreementRow(
                agreed = agreed,
                onAgreedChange = { agreed = it }
            )
            Spacer(modifier = Modifier.height(12.dp))
            LoadingButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        if (canNext) Colors.black_101828 else Colors.gray_B1B8C6
                    )
                    .padding(vertical = 14.dp),
                text = "下一步",
                isLoading = nextLoading,
                onClick = {
                    if (!canNext || nextLoading) return@LoadingButton
                    nextLoading = true
                    // TODO: 调起支付 / 下单，结束后 nextLoading = false
                    nextLoading = false
                }
            )
        }
    }
}

@Composable
private fun PointRechargeViewPreview() {
    PointRechargeView(onBackClick = {})
}
