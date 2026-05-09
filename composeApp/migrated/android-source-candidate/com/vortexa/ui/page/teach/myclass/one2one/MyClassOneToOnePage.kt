package com.vortexa.ui.page.teach.myclass.one2one

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

/**
 * 一对一服务 Tab 内容页：上方 Filter（全部/待接受/待完成/已取消/已完成）+ 下方列表（服务端按 status 筛选）。
 *
 * @param items 当前筛选后的列表数据
 * @param selectedFilterIndex 当前选中的筛选下标，与 [MyClassOneToOneFilterOptions] 一致
 * @param onFilterIndexChange 筛选变更回调
 * @param onItemMoreClick 某项更多点击，参数为下标
 * @param onItemButtonClick 某项底部按钮点击，参数为下标
 */
@Composable
fun MyClassOneToOnePage(
    items: List<MyClassOneToOneItemUi>,
    selectedFilterIndex: Int = 0,
    onFilterIndexChange: (Int) -> Unit = {},
    onItemMoreClick: (Int) -> Unit = {},
    onItemButtonClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MyClassOneToOneFilter(
            selectedIndex = selectedFilterIndex,
            onOptionClick = onFilterIndexChange
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(
                items,
                key = { index, item -> index }
            ) { index, item ->
                MyClassOneToOneListItem(
                    item = item,
                    onMoreClick = { onItemMoreClick(index) },
                    onButtonClick = { onItemButtonClick(index) }
                )
            }
        }
    }
}

@Composable
@Preview
private fun MyClassOneToOnePagePreview() {
    MyClassOneToOnePage(
        items = listOf(
            MyClassOneToOneItemUi(
                reserveId = 66666L,
                status = "待完成",
                startTime = "2025-12-21 16:00:00",
                bookTime = "2025-12-21 20:00:00",
                studentName = "步惊云",
                teacherName = "秦霜",
                teacherId = 1L,
                duration = "2小时"
            ),
            MyClassOneToOneItemUi(
                reserveId = 66667L,
                status = "已完成",
                startTime = "2025-12-22 10:00:00",
                bookTime = "2025-12-22 10:00:00",
                studentName = "聂风",
                teacherName = "雄霸",
                teacherId = 2L,
                duration = "1小时"
            ),
            MyClassOneToOneItemUi(
                reserveId = 66668L,
                status = "已取消",
                startTime = "2025-12-23 14:00:00",
                bookTime = "2025-12-23 14:00:00",
                studentName = "无名",
                teacherName = "剑晨",
                teacherId = 3L,
                duration = "2小时"
            )
        ),
        selectedFilterIndex = 0,
        onFilterIndexChange = {}
    )
}
