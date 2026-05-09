package com.vortexa.ui.page.wallet.pay

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.util.extension.click

/**
 * 垂直排布的支付方式单选列表：微信、支付宝、Web3 钱包。
 */
@Composable
fun PointRechargePayChannelList(
    selected: PointRechargePayChannel,
    onSelect: (PointRechargePayChannel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PointRechargePayChannel.entries.forEach { channel ->
            val isSelected = channel == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Colors.blue_3266FF else Colors.gray_E5E8EB,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(if (isSelected) Colors.gray_f0f4fe else Color.White)
                    .click(onClickListener = { onSelect(channel) })
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(channel.iconRes),
                        contentDescription = channel.label,
                        modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = channel.label,
                        style = FontMedium(fontSize = 15, color = Colors.black_101828)
                    )
                }
                RadioButton(
                    selected = isSelected,
                    onClick = { onSelect(channel) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Colors.blue_3266FF,
                        unselectedColor = Colors.gray_B1B8C6
                    )
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun PointRechargePayChannelListPreview() {
    PointRechargePayChannelList(
        selected = PointRechargePayChannel.Alipay,
        onSelect = {}
    )
}
