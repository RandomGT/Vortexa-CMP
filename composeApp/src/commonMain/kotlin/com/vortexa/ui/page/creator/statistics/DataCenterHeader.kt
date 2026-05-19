package com.vortexa.ui.page.creator.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
import org.jetbrains.compose.resources.painterResource
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.ic_arrow_left

@Composable
fun DataCenterHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_left),
            contentDescription = "返回",
            tint = Colors.black_101828,
            modifier = Modifier
                .size(24.dp)
                .click(onClickListener = onBackClick),
        )
        Text(
            text = "数据中心",
            style = FontMedium(fontSize = 16, color = Colors.black_101828),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.size(24.dp))
    }
}
