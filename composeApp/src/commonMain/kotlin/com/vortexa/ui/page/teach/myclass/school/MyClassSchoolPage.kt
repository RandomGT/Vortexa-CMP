package com.vortexa.ui.page.teach.myclass.school

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.model.SchoolCourseCard
import com.vortexa.ui.component.DefaultColorLabel
import com.vortexa.ui.page.teach.myclass.MyClassViewModel
import com.vortexa.ui.page.teach.myclass.SchoolFilterPopupStyle
import com.vortexa.ui.viewmodel.vortexaViewModel

/** Filter 选中项对应的 DefaultColorLabel 文案：first、last */
private val filterLabelParts = listOf(
    "知识" to "读物",
    "视频" to "教学"
)

/**
 * 涡联学院页：顶部 Filter、中间 DefaultColorLabel（随 Filter 变化）、底部两列 Grid 列表；
 * Filter 右侧菜单按钮点击后显示学员/导师浮窗，由 ViewModel Flow 控制。
 *
 * @param items 课程卡片列表，可由 ViewModel 注入
 * @param viewModel 用于浮窗显隐与样式的 ViewModel
 */
@Composable
fun MyClassSchoolPage(
    items: List<SchoolCourseCard> = emptyList(),
    viewModel: MyClassViewModel = vortexaViewModel { MyClassViewModel() }
) {
    var selectedFilterIndex by remember { mutableStateOf(0) }
    val (labelFirst, labelLast) = filterLabelParts.getOrElse(selectedFilterIndex) { "知识" to "读物" }

    val showFilterPopup by viewModel.showFilterPopup.collectAsState()
    val filterPopupStyle by viewModel.filterPopupStyle.collectAsState()
    val schoolMenuSelectedIndex by viewModel.schoolMenuSelectedIndex.collectAsState()

    val popupOptions = if (filterPopupStyle == SchoolFilterPopupStyle.TUTOR) {
        SchoolFilterPopupOptionsTutor
    } else {
        SchoolFilterPopupOptionsStudent
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MyClassSchoolFilter(
                selectedIndex = selectedFilterIndex,
                onOptionClick = { selectedFilterIndex = it },
                onMenuClick = { viewModel.openFilterPopup() }
            )
            DefaultColorLabel(
                first = labelFirst,
                last = labelLast,
                showMore = true,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
            MyClassSchoolGrid(
                items = items,
                onOffShelfClick = { },
                modifier = Modifier.weight(1f)
            )
        }

        if (showFilterPopup) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { viewModel.dismissFilterPopup() },
                contentAlignment = Alignment.TopEnd
            ) {
                MyClassSchoolFilterPopup(
                    options = popupOptions,
                    selectedIndex = schoolMenuSelectedIndex,
                    onOptionClick = { viewModel.selectSchoolMenuOption(it) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 50.dp, end = 18.dp)
                )
            }
        }
    }
}

/**
 * 两列 Grid 列表，Figma 788-73189 卡片样式；水平间距 14dp，行间距 24dp。
 */
@Composable
private fun MyClassSchoolGrid(
    items: List<SchoolCourseCard>,
    onOffShelfClick: (SchoolCourseCard) -> Unit,
    modifier: Modifier = Modifier
) {
    val horizontalPadding = 18.dp
    val itemSpacing = 14.dp
    val verticalSpacing = 24.dp
    val rows = items.chunked(2)

    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
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
                    MyClassSchoolCardItem(
                        card = card,
                        onOffShelfClick = { onOffShelfClick(card) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowCards.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
@Preview
private fun MyClassSchoolPagePreview() {
    MyClassSchoolPage(
        items = listOf(
            SchoolCourseCard(
                id = "1",
                title = "量化交易一阶课程",
                teacherName = "",
                purchaseCount = "30",
                tags = listOf("量化交易", "短线"),
                price = 0f,
                unit = "",
                rating = 0f
            ),
            SchoolCourseCard(
                id = "1",
                title = "量化交易一阶课程",
                teacherName = "",
                purchaseCount = "30",
                tags = listOf("量化交易", "短线"),
                price = 0f,
                unit = "",
                rating = 0f
            )
            ,
            SchoolCourseCard(
                id = "1",
                title = "量化交易一阶课程",
                teacherName = "",
                purchaseCount = "30",
                tags = listOf("量化交易", "短线"),
                price = 0f,
                unit = "",
                rating = 0f
            ),
            SchoolCourseCard(
                id = "1",
                title = "量化交易一阶课程",
                teacherName = "",
                purchaseCount = "30",
                tags = listOf("量化交易", "短线"),
                price = 0f,
                unit = "",
                rating = 0f
            ),SchoolCourseCard(
                id = "1",
                title = "量化交易一阶课程",
                teacherName = "",
                purchaseCount = "30",
                tags = listOf("量化交易", "短线"),
                price = 0f,
                unit = "",
                rating = 0f
            ),SchoolCourseCard(
                id = "1",
                title = "量化交易一阶课程",
                teacherName = "",
                purchaseCount = "30",
                tags = listOf("量化交易", "短线"),
                price = 0f,
                unit = "",
                rating = 0f
            )
        )
    )
}
