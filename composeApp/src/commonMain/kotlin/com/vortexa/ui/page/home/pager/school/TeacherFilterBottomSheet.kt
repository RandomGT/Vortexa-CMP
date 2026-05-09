package com.vortexa.ui.page.home.pager.school

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.ToastUtil
import com.vortexa.util.extension.click

/** Figma 283-30225：导师筛选弹窗可选标签（与接口 tags 对应） */
private val FILTER_TAG_OPTIONS = listOf(
    "交易经验", "项目咨询", "空投策略", "市场&投资", "应用", "更多"
)

/** 底部 Tab 栏高度，弹窗内容需留出该空间 */
private const val TAB_BAR_HEIGHT_DP = 50

/**
 * 导师筛选弹窗（Figma 283-30225）：从底部弹出，含「导师分类」多选 chips、「报价」Min/Max 输入、「确认」按钮。
 * @param initialSelectedTags 初始已选标签
 * @param initialMinPrice 初始最低报价
 * @param initialMaxPrice 初始最高报价
 * @param onDismiss 关闭弹窗
 * @param onConfirm 确认筛选，参数为选中标签集合、最低报价、最高报价
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherFilterBottomSheet(
    initialSelectedTags: Set<String> = emptySet(),
    initialMinPrice: String = "",
    initialMaxPrice: String = "",
    onDismiss: () -> Unit = {},
    onConfirm: (Set<String>, String, String) -> Unit = { _, _, _ -> }
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxWidth(),
        containerColor = Colors.gray_F8F9FA.copy(alpha = 0f),
        dragHandle = null
    ) {
        TeacherFilterSheetContent(
            initialSelectedTags = initialSelectedTags,
            initialMinPrice = initialMinPrice,
            initialMaxPrice = initialMaxPrice,
            onConfirm = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

/**
 * 弹窗内容：导师分类 chips、报价 Min/To/Max、确认按钮。Figma 圆角 20、间距 18/10、chip 8dp 圆角。
 */
@Composable
private fun TeacherFilterSheetContent(
    initialSelectedTags: Set<String>,
    initialMinPrice: String,
    initialMaxPrice: String,
    onConfirm: (Set<String>, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTags by remember(initialSelectedTags) {
        mutableStateOf(initialSelectedTags.toMutableSet())
    }
    var minPrice by remember(initialMinPrice) { mutableStateOf(initialMinPrice) }
    var maxPrice by remember(initialMaxPrice) { mutableStateOf(initialMaxPrice) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(Colors.gray_F8F9FA)
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "导师分类",
                style = FontMedium(fontSize = 18, color = Colors.black_101828),
                modifier = Modifier.padding(top = 16.dp, bottom = 0.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FILTER_TAG_OPTIONS.take(3).forEach { tag ->
                    FilterChip(
                        text = tag,
                        selected = tag in selectedTags,
                        onClick = {
                            selectedTags = (if (tag in selectedTags) selectedTags - tag else selectedTags + tag) as MutableSet<String>
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FILTER_TAG_OPTIONS.drop(3).forEach { tag ->
                    FilterChip(
                        text = tag,
                        selected = tag in selectedTags,
                        onClick = {
                            selectedTags = (if (tag in selectedTags) selectedTags - tag else selectedTags + tag) as MutableSet<String>
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Text(
                "报价",
                style = FontRegular(fontSize = 14, color = Colors.black_101828),
                modifier = Modifier.padding(top = 16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                PriceField(
                    value = minPrice,
                    onValueChange = { minPrice = it },
                    placeholder = "Min",
                    modifier = Modifier.weight(1f)
                )
                Text("To", style = FontRegular(fontSize = 14, color = Colors.black_101828))
                PriceField(
                    value = maxPrice,
                    onValueChange = { maxPrice = it },
                    placeholder = "Max",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Colors.black_101828)
                .click(onClickListener = {
                    val minStr = minPrice.trim()
                    val maxStr = maxPrice.trim()
                    val minVal = minStr.toDoubleOrNull()
                    val maxVal = maxStr.toDoubleOrNull()
                    if (minVal != null && maxVal != null && minVal > maxVal) {
                        ToastUtil.show("最小值不能大于最大值")
                    } else {
                        onConfirm(selectedTags, minStr, maxStr)
                    }
                }),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("确认", style = FontMedium(fontSize = 16, color = Color.White))
        }
    }
}

@Composable
private fun TeacherFilterSheetContentPreview() {
    TeacherFilterSheetContent(
        initialSelectedTags = setOf("交易经验"),
        initialMinPrice = "",
        initialMaxPrice = "",
        onConfirm = { _, _, _ -> }
    )
}
