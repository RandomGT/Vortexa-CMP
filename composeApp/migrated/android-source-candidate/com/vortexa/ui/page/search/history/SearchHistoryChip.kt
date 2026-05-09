package com.vortexa.ui.page.search.history


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click

private const val HISTORY_CHIP_MAX_CHARS = 6

private val CHIP_HEIGHT = 32.dp

private fun String.toHistoryChipLabel(): String =
    if (length <= HISTORY_CHIP_MAX_CHARS) this
    else take(HISTORY_CHIP_MAX_CHARS) + "..."
/**
 * 单条搜索历史芯片：灰底圆角，点击回填并触发提交。
 */
@Composable
fun SearchHistoryChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(CHIP_HEIGHT)
            .clip(RoundedCornerShape(8.dp))
            .background(Colors.gray_F8F9FA)
            .click(onClickListener = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.toHistoryChipLabel(),
            style = FontRegular(fontSize = 14, color = Colors.black_101828),
            maxLines = 1
        )
    }
}