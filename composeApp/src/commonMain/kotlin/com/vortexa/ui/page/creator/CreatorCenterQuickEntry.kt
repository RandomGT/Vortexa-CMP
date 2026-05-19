package com.vortexa.ui.page.creator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

data class CreatorQuickEntryItem(
    val label: String,
    val iconResId: DrawableResource,
    val onClick: () -> Unit,
)

@Composable
fun CreatorCenterQuickEntry(
    entries: List<CreatorQuickEntryItem>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Colors.gray_F8F9FA)
            .padding(horizontal = 18.dp, vertical = 20.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
    ) {
        entries.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .click(item.onClick),
            ) {
                Icon(
                    painter = painterResource(item.iconResId),
                    contentDescription = item.label,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = item.label,
                    style = FontRegular(fontSize = 14, color = Colors.black_101828),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
