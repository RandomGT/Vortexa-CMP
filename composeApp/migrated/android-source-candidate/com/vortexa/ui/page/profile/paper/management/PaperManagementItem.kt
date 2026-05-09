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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vortexa.ui.component.AvatarImage
import com.vortexa.ui.theme.Colors
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res

/** 稿件列表项数据 */
data class PaperItemData(
    val postId: Long = 0L,
    /** 发布板块，编辑发帖页预填；接口未返回时可为 null */
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

/** 底部操作按钮配置，支持 1～3 个按钮均分宽度 */
data class PaperItemButton(
    val text: String,
    val isPrimary: Boolean,
)

/**
 * 稿件管理列表单项
 * 包含头像/姓名/状态/时间、标题与描述、点赞评论数、底部操作按钮（均分空间）
 *
 * @param item 稿件数据
 * @param buttons 底部按钮列表（数量可变，每个按钮等分宽度）
 * @param buttons 预留与列表配置对齐（当前底部按钮在组件内写死）
 * @param onDeleteClick 点击「删除」时由列表展示二次确认弹窗
 */
@Composable
fun PaperManagementItem(
    item: PaperItemData,
    buttons: List<PaperItemButton>,
    onDeleteClick: () -> Unit = {},
) {
    val cardShape = RoundedCornerShape(16.dp)
    val viewModel = viewModel(PaperManagementViewModel::class)
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Colors.gray_F8F9FA, cardShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 顶部：头像 + 姓名/状态 + 时间（可点击进入详情，与底部操作按钮区分）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .click {
                    if (item.postId > 0L) {
                        viewModel.onOpenPostDetail(context, item)
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
            // 标题
            Text(
                text = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Colors.black_101828,
            )
            // 分隔线（Figma: container-profile 底边 / button-action 顶边）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(1.dp)
                    .background(Colors.gray_f0f4fe),
            )
            // 描述 + 点赞/评论
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

        // 底部按钮：均分宽度，数量可变
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PaperActionButton(PaperItemButton("数据", false)) {
                viewModel.onOpenDataCenter(context)
            }
            PaperActionButton(PaperItemButton("删除", false)) {
                if (item.postId > 0L) onDeleteClick()
            }
            PaperActionButton(PaperItemButton("编辑", true)) {
                viewModel.onEditClick(item, context)
            }
        }
    }
}

@Composable
private fun RowScope.PaperActionButton(
    btn: PaperItemButton,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .background(
                color = if (btn.isPrimary) Colors.black_101828 else Colors.gray_EEF0F1,
                shape = RoundedCornerShape(30.dp),
            )
            .click(onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = btn.text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (btn.isPrimary) Color.White else Colors.black_101828,
        )
    }
}

@Composable
@Preview
private fun PaperManagementItemPreview() {
    PaperManagementItem(
        item = PaperItemData(
            name = "Kaelani Silvermoon",
            statusText = "审核中",
            dateText = "2025-09-15  09:00:23",
            title = "关于比特币：难忘的瞬间",
            description = "关于比特币、区块链和加密货币趋势的最新见解。当夕阳西下，将天空染成金色和深红色的色调时，一种宁静的感觉涌上心头。",
            likeCount = "163K",
            commentCount = "12K",
        ),
        buttons = listOf(
            PaperItemButton("数据", isPrimary = false),
            PaperItemButton("删除", isPrimary = false),
            PaperItemButton("编辑", isPrimary = true),
        ),
    )
}
