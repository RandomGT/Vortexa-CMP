package com.vortexa.ui.page.post.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.vortexa.ui.component.PopupDropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.ic_chevron_down

/**
 * 发布页顶部操作栏（Figma 278-24774）。
 * 左侧「发布到」+ 模块下拉，右侧「发帖」按钮。
 *
 * @param selectedModuleIndex 当前选中的板块索引
 * @param onModuleSelect 选择板块回调
 * @param onPublishClick 点击发帖回调
 * @param isPublishing 是否发布中，为 true 时禁用发帖按钮
 */
@Composable
fun PostCreateTopBar(
    selectedModuleIndex: Int,
    onModuleSelect: (Int) -> Unit,
    onPublishClick: () -> Unit,
    isPublishing: Boolean = false,
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .padding(horizontal = 18.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "发布到",
                style = FontMedium(fontSize = 16, color = Colors.black_101828)
            )
            Box(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.width(147.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Colors.gray_EEF0F1)
                            .clickable { dropdownExpanded = true }
                            .padding(horizontal = 8.dp, vertical = 3.5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = POST_CREATE_MODULES.getOrNull(selectedModuleIndex) ?: "选择模块",
                            style = FontRegular(fontSize = 12, color = Colors.gray_6A7282)
                        )
                        Icon(
                            painter = painterResource(Res.drawable.ic_chevron_down),
                            contentDescription = "展开",
                            modifier = Modifier.size(16.dp),
                            tint = Colors.gray_6A7282
                        )
                    }
                    PopupDropdownMenu(
                        modifier = Modifier.width(147.dp),
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        options = POST_CREATE_MODULES,
                        onOptionClick = {
                            onModuleSelect(it)
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(if (isPublishing) Colors.gray_B1B8C6 else Colors.black_101828)
                .clickable(enabled = !isPublishing, onClick = onPublishClick)
                .padding(horizontal = 16.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "发帖",
                style = FontMedium(fontSize = 12, color = Color.White)
            )
        }
    }
}

@Composable
private fun PostCreateTopBarPreview() {
    PostCreateTopBar(
        selectedModuleIndex = 0,
        onModuleSelect = {},
        onPublishClick = {}
    )
}
