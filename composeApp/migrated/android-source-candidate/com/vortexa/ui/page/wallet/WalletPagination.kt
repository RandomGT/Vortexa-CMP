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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.util.extension.click

/**
 * 底部分页组件（Figma 336-21437）：上一页 | 当前页/总页数 | 下一页，白底、圆角按钮。
 *
 * @param currentPage 当前页（1-based）
 * @param totalPages 总页数
 * @param onPrevClick 点击「上一页」
 * @param onNextClick 点击「下一页」
 */
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
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(0.5f)
                    .background(Colors.gray_EEF0F1, RoundedCornerShape(30.dp))
                    .padding(horizontal = 24.dp, vertical = 10.dp)
                    .click(onClickListener = onPrevClick),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "上一页",
                    style = FontMedium(fontSize = 16, color = Colors.black_101828)
                )
            }
            Text(
                modifier = Modifier.weight(0.5f),
                textAlign = TextAlign.Center,
                text = "$currentPage/$totalPages",
                style = FontMedium(fontSize = 16, color = Colors.black_101828)
            )
            Row(
                modifier = Modifier
                    .weight(0.5f)
                    .background(Colors.gray_EEF0F1, RoundedCornerShape(30.dp))
                    .padding(horizontal = 24.dp, vertical = 10.dp)
                    .click(onClickListener = onNextClick),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "下一页",
                    style = FontMedium(fontSize = 16, color = Colors.black_101828)
                )
            }
        }
    }
}

@Composable
@Preview
private fun WalletPaginationPreview() {
    WalletPagination(
        currentPage = 2,
        totalPages = 5,
        onPrevClick = {},
        onNextClick = {}
    )
}
