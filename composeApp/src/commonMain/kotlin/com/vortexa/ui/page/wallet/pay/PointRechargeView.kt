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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vortexa.ui.component.LoadingButton
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.ui.theme.belowStatusBar
import com.vortexa.ui.viewmodel.vortexaViewModel

@Composable
fun PointRechargeView(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PointRechargeViewModel = vortexaViewModel { PointRechargeViewModel() }
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.toastMessage) {
        state.toastMessage ?: return@LaunchedEffect
        viewModel.consumeToast()
    }

    val points = state.amountText.toIntOrNull() ?: 0
    val canSubmit = points > 0 && state.agreed

    Column(
        modifier = modifier
            .fillMaxSize()
            .belowStatusBar()
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
                text = "${state.balance}",
                style = FontMedium(fontSize = 28, color = Colors.black_101828)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "充值积分数量",
                style = FontMedium(fontSize = 14, color = Colors.black_101828)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.amountText,
                onValueChange = viewModel::onAmountChange,
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
                selected = state.selectedChannel,
                onSelect = viewModel::onChannelSelect
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
                agreed = state.agreed,
                onAgreedChange = viewModel::onAgreedChange
            )
            Spacer(modifier = Modifier.height(12.dp))
            LoadingButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(if (canSubmit) Colors.black_101828 else Colors.gray_B1B8C6)
                    .padding(vertical = 14.dp),
                text = "下一步",
                isLoading = state.submitLoading,
                onClick = viewModel::submit
            )
        }
    }
}
