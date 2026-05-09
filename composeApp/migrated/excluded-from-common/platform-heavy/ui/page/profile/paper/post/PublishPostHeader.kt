package com.vortexa.ui.page.profile.paper.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.ic_chevron_down

/**
 * 发布页顶部栏（Figma 504-56281）：左侧「发布到」+ 可见性下拉（公开/私密），右侧「发帖」按钮。
 *
 * @param visibility 当前可见性选项
 * @param onVisibilityChange 选择可见性回调
 * @param onPublishClick 点击发帖回调
 */
@Composable
fun PublishPostHeader(
    visibility: PublishVisibility,
    onVisibilityChange: (PublishVisibility) -> Unit,
    onPublishClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 左侧：「发布到」+ 下拉
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
                Row(
                    modifier = Modifier
                        .width(147.dp)
                        .background(
                            color = Colors.gray_F8F9FA,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.5.dp)
                        .clickable { dropdownExpanded = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = visibility.label,
                        style = FontRegular(fontSize = 12, color = Colors.black_101828)
                    )
                    Icon(
                        painter = painterResource(Res.drawable.ic_chevron_down),
                        contentDescription = "展开",
                        modifier = Modifier.size(16.dp),
                        tint = Colors.black_101828
                    )
                }
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    PublishVisibility.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.label,
                                    style = FontRegular(fontSize = 14, color = Colors.black_101828)
                                )
                            },
                            onClick = {
                                onVisibilityChange(option)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 右侧：发帖按钮（胶囊形）
        Row(
            modifier = Modifier
                .background(
                    color = Colors.black_101828,
                    shape = RoundedCornerShape(100.dp)
                )
                .padding(horizontal = 16.dp, vertical = 5.dp)
                .clickable(onClick = onPublishClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "发帖",
                style = FontMedium(fontSize = 12, color = androidx.compose.ui.graphics.Color.White)
            )
        }
    }
}

/** 发布可见性：公开 / 私密 */
enum class PublishVisibility(val label: String) {
    Public("公开"),
    Private("私密")
}

@Composable
private fun PublishPostHeaderPreview() {
    PublishPostHeader(
        visibility = PublishVisibility.Public,
        onVisibilityChange = {},
        onPublishClick = {}
    )
}
