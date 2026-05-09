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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vortexa.ui.theme.Colors
import com.vortexa.util.extension.click

/**
 * 筛选栏组件
 * 显示筛选选项列表
 */
@Composable
fun PaperManagementFilter() {
    val viewModel = viewModel(PaperManagementViewModel::class.java)
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        itemsIndexed(viewModel.paperFilters) { index, filter ->
            FilterItem(text = filter, index = index, isSelected = selectedFilter == index)
        }
    }
}

/**
 * 单个筛选标签项
 * @param text 标签文本
 * @param isSelected 是否选中
 */
@Composable
fun FilterItem(text: String, index: Int, isSelected: Boolean) {
    val viewModel = viewModel(PaperManagementViewModel::class.java)
    Text(
        text = text,
        color = if (isSelected) Color.White else Colors.gray_667085,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .background(
                color = if (isSelected) Colors.black_101828 else Colors.gray_F3F5F7,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .click {
                viewModel.onFilterClick(index)
            }
    )
}
