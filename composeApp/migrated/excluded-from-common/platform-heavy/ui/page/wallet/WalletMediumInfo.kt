package com.vortexa.ui.page.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vortexa.ui.theme.Colors

@Composable
fun WalletMediumInfo() {
    Column(modifier = Modifier.padding(top = 15.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "我的收入",
                modifier = Modifier.padding(start = 20.dp),
                style = TextStyle(color = Colors.black_101828, fontSize = 20.sp)
            )
            Spacer(modifier = Modifier.weight(1f))

            Text(
                "申请提现", modifier = Modifier
                    .padding(end = 20.dp)
                    .background(Colors.black_101828, RoundedCornerShape(15.dp))
                    .padding(horizontal = 15.dp, vertical = 5.dp),
                style = TextStyle(color = Color.White, fontSize = 14.sp)
            )
        }

        Row(
            modifier = Modifier
                .padding(top = 15.dp)
                .padding(horizontal = 20.dp)
                .background(Colors.gray_EEF0F1, RoundedCornerShape(10.dp))
                .fillMaxWidth()
                .height(70.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "2,500",
                    style = TextStyle(color = Colors.black_101828, fontSize = 18.sp)
                )
                Text(
                    "涡联币总额",
                    style = TextStyle(color = Colors.black_101828, fontSize = 12.sp)
                )
            }

            VerticalDivider(
                modifier = Modifier
                    .padding(vertical = 15.dp)
                    .fillMaxHeight()
                    .alpha(0.5f)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "2,500",
                    style = TextStyle(color = Colors.black_101828, fontSize = 18.sp)
                )
                Text(
                    "涡联币总额",
                    style = TextStyle(color = Colors.black_101828, fontSize = 12.sp)
                )
            }

            VerticalDivider(
                modifier = Modifier
                    .padding(vertical = 15.dp)
                    .fillMaxHeight()
                    .alpha(0.5f)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "2,500",
                    style = TextStyle(color = Colors.black_101828, fontSize = 18.sp)
                )
                Text(
                    "涡联币总额",
                    style = TextStyle(color = Colors.black_101828, fontSize = 12.sp)
                )
            }
        }
    }
}