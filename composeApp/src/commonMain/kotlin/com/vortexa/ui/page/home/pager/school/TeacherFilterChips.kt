package com.vortexa.ui.page.home.pager.school

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click

/**
 * 筛选标签 Chip（Figma：选中 #101828+白字，未选 #F8F9FA+#101828）。
 */
@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) Colors.black_101828 else Colors.gray_F8F9FA
    val textColor = if (selected) Color.White else Colors.black_101828
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 16.dp, vertical = 11.dp)
            .click(onClickListener = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text, style = FontRegular(fontSize = 14, color = textColor))
    }
}

/**
 * 报价输入框（Figma 占位 #B1B8C6，8dp 圆角）。
 */
@Composable
fun PriceField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Colors.gray_F8F9FA)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        textStyle = FontRegular(fontSize = 14, color = Colors.black_101828),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        cursorBrush = SolidColor(Colors.black_101828),
        singleLine = true,
        decorationBox = { inner ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (value.isEmpty()) {
                    Text(placeholder, style = FontRegular(fontSize = 14, color = Colors.gray_b1b8c6))
                }
                inner()
            }
        }
    )
}

@Composable
private fun FilterChipPreview() {
    Row() {
        FilterChip("交易经验", selected = true, onClick = {})
        FilterChip("项目咨询", selected = false, onClick = {})
    }
}
