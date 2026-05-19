package com.vortexa.ui.page.profile.paper.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import com.vortexa.ui.component.AvatarImage
import com.vortexa.ui.theme.Colors
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.heart_small
import vortexa.composeapp.generated.resources.message_circle

/** 稿件列表项数据。 */
data class PaperItemData(
    val postId: Long = 0L,
    val board: String? = null,
    val avatarUrl: String? = null,
    val name: String,
    val statusText: String,
    val dateText: String,
    val title: String,
    val description: String,
    val content: String = description,
    val imageResources: List<String> = emptyList(),
    val videoResources: List<String> = emptyList(),
    val likeCount: String,
    val commentCount: String,
)

/** 底部操作按钮配置，支持 1～3 个按钮均分宽度。 */
data class PaperItemButton(
    val text: String,
    val isPrimary: Boolean,
)

/**
 * 稿件管理列表单项。
 */
@Composable
fun PaperManagementItem(
    item: PaperItemData,
    buttons: List<PaperItemButton>,
    onPostClick: (PaperItemData) -> Unit = {},
    onDataClick: (PaperItemData) -> Unit = {},
    onDeleteClick: (PaperItemData) -> Unit = {},
    onEditClick: (PaperItemData) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Colors.gray_F8F9FA, cardShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .click {
                    if (item.postId > 0L) {
                        onPostClick(item)
                    }
                },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AvatarImage(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Colors.gray_F3F5F7),
                    avatarUrl = item.avatarUrl,
                    contentDescription = "${item.name}头像",
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = item.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Colors.black_101828,
                        )
                        Text(
                            text = item.statusText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Colors.blue_277DFF,
                        )
                    }
                    Text(
                        text = item.dateText,
                        fontSize = 11.sp,
                        color = Colors.gray_6A7282,
                    )
                }
            }
            Text(
                text = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Colors.black_101828,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(1.dp)
                    .background(Colors.gray_f0f4fe),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Text(
                    text = item.description,
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    color = Colors.gray_6A7282,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.Bottom)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.heart_small),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Colors.gray_6A7282,
                        )
                        Text(
                            text = item.likeCount,
                            fontSize = 14.sp,
                            color = Colors.gray_6A7282,
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.message_circle),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Colors.gray_6A7282,
                        )
                        Text(
                            text = item.commentCount,
                            fontSize = 14.sp,
                            color = Colors.gray_6A7282,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            buttons.forEach { button ->
                PaperActionButton(button) {
                    when (button.text) {
                        "数据" -> onDataClick(item)
                        "删除" -> if (item.postId > 0L) onDeleteClick(item)
                        "编辑" -> if (item.postId > 0L) onEditClick(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.PaperActionButton(
    button: PaperItemButton,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .background(
                color = if (button.isPrimary) Colors.black_101828 else Colors.gray_EEF0F1,
                shape = RoundedCornerShape(30.dp),
            )
            .click(onClickListener = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = button.text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (button.isPrimary) Color.White else Colors.black_101828,
        )
    }
}
