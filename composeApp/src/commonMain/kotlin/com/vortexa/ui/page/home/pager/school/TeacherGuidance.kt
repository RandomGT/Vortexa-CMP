package com.vortexa.ui.page.home.pager.school

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.ui.component.DefaultColorLabel
import com.vortexa.ui.component.ListEndFooter
import com.vortexa.ui.page.home.pager.home.recommend.RecommendCardItem
import com.vortexa.ui.theme.BaseTheme
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.icon_menu_blue

/**
 * 导师在线指导页：通过 ViewModel 订阅 mentorCards；点击筛选 Icon 从底部弹出筛选弹窗，确认后刷新列表。
 *
 * @param listState 外部传入的列表滚动状态，用于与折叠头部容器联动。
 * @param viewModel 页面 ViewModel。
 * @return 无返回值。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherGuidance(
    listState: LazyListState,
    viewModel: SchoolViewModel = vortexaViewModel { SchoolViewModel() }
) {
    val mentorCards by viewModel.mentorCards.collectAsState()
    val filterTags by viewModel.filterTags.collectAsState()
    val filterMinPrice by viewModel.filterMinPrice.collectAsState()
    val filterMaxPrice by viewModel.filterMaxPrice.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    if (showFilterSheet) {
        TeacherFilterBottomSheet(
            initialSelectedTags = filterTags,
            initialMinPrice = filterMinPrice,
            initialMaxPrice = filterMaxPrice,
            onDismiss = { showFilterSheet = false },
            onConfirm = { tags, min, max ->
                viewModel.applyFilter(tags, min, max)
                showFilterSheet = false
            }
        )
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        item {
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(Res.drawable.icon_menu_blue),
                    contentDescription = "筛选",
                    modifier = Modifier
                        .rotate(180f)
                        .size(20.dp)
                        .click(onClickListener = { showFilterSheet = true })
                )
                DefaultColorLabel(
                    "导师",
                    "分类",
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .weight(1f),
                    showMore = false
                )
            }
        }
        val horizontalPadding = 16.dp
        val itemSpacing = 14.dp
        val rows = mentorCards.chunked(2)
        itemsIndexed(
            rows,
            key = { index, _ -> index }
        ) { index, rowCards ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        top = if (index == 0) 16.dp else itemSpacing
                    ),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {
                rowCards.forEach { card ->
                    RecommendCardItem(
                        card = card,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowCards.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        item {
            ListEndFooter(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 24.dp)
            )
        }
    }
}

/**
 * 预览导师在线指导列表布局。
 *
 * @return 无返回值。
 */
@Composable
private fun TeacherGuidancePreview() {
    BaseTheme {
        TeacherGuidance(listState = rememberLazyListState())
    }
}
