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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.ui.theme.BaseTheme
import com.vortexa.ui.component.DefaultColorLabel
import com.vortexa.ui.component.ListEndFooter
import kotlin.collections.chunked
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.icon_menu_blue

/**
 * 涡联学院/有声读物列表：Header + 两列 Grid，数据来自 SchoolViewModel.schoolListCards。
 *
 * @param listState 外部传入的列表滚动状态，用于与折叠头部容器联动。
 * @param viewModel 页面 ViewModel。
 * @return 无返回值。
 */
@Composable
fun VortexaSchoolList(
    listState: LazyListState,
    viewModel: SchoolViewModel = vortexaViewModel { SchoolViewModel() }
) {
    val schoolListCards by viewModel.schoolListCards.collectAsState()

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
                    contentDescription = null,
                    modifier = Modifier
                        .rotate(180f)
                        .size(20.dp)
                )
                DefaultColorLabel(
                    "有声",
                    "读物",
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .weight(1f),
                    showMore = true
                )
            }
        }
        val horizontalPadding = 18.dp
        val itemSpacing = 14.dp
        val verticalSpacing = 24.dp
        val rows = schoolListCards.chunked(2)
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
                        top = if (index == 0) 16.dp else verticalSpacing
                    ),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {
                rowCards.forEach { card ->
                    VortexaSchoolCardItem(
                        card = card,
                        modifier = Modifier.weight(1f)
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
 * 预览涡联学院列表布局。
 *
 * @return 无返回值。
 */
@Composable
private fun VortexaSchoolListPreview() {
    BaseTheme {
        VortexaSchoolList(listState = rememberLazyListState())
    }
}
