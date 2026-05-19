package com.vortexa.ui.page.profile.paper.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vortexa.ui.theme.Colors
import com.vortexa.util.extension.click

/**
 * 稿件筛选栏。
 */
@Composable
fun PaperManagementFilter(
    filters: List<String>,
    selectedIndex: Int,
    onFilterClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        itemsIndexed(filters) { index, filter ->
            FilterItem(
                text = filter,
                isSelected = selectedIndex == index,
                onClick = { onFilterClick(index) }
            )
        }
    }
}

@Composable
fun FilterItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = if (isSelected) Color.White else Colors.gray_667085,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier
            .background(
                color = if (isSelected) Colors.black_101828 else Colors.gray_F3F5F7,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .click(onClickListener = onClick)
    )
}
