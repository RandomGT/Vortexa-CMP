package com.vortexa.ui.page.teach.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium

@Composable
internal fun ClassAssistantBottomCapsule(
    text: String,
    dark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(30.dp),
    enabled: Boolean = true
) {
    val bg = if (dark) Colors.black_101828 else Colors.gray_EEF0F1
    val fg = if (dark) Color.White else Colors.black_101828
    val contentColor = if (enabled) fg else fg.copy(alpha = 0.45f)
    Row(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .padding(vertical = 10.dp)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = FontMedium(fontSize = 16, color = contentColor))
    }
}

@Preview
@Composable
private fun ClassAssistantBottomCapsulePreview() {
    ClassAssistantBottomCapsule(text = "接受", dark = true, onClick = {})
}
