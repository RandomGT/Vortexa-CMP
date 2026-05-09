package com.vortexa.ui.page.creator.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.model.CreatorStatisticsPostItem
import com.vortexa.ui.component.ListEndFooter
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res

/**
 * 在 LazyListScope 内添加数据中心帖子列表区块（Figma 504-51175）：
 * 标题行（数据详情 + 排序菜单）；时间范围仅在上半部分「数据概览」中选择。
 * 数据来源：GET /v/api/user/posts/data/{days}
 *
 * @param selectedSortBy 当前选中的排序方式（与 [DataCenterSortByOptions] / 接口一致）
 * @param list 帖子统计列表
 * @param pageStatus 页面状态（成功且列表非空时才展示加载更多 / 到底）
 * @param hasMorePosts 是否还有下一页
 * @param loadingMorePosts 是否正在加载下一页
 * @param onSortByChange 切换排序回调
 * @param onItemClick 点击帖子项回调
 */
fun LazyListScope.dataCenterPostListSection(
    selectedSortBy: Int,
    list: List<CreatorStatisticsPostItem>,
    pageStatus: PageStatus,
    hasMorePosts: Boolean,
    loadingMorePosts: Boolean,
    onSortByChange: (Int) -> Unit,
    onItemClick: (CreatorStatisticsPostItem) -> Unit
) {
    // 卡片头部（圆角 + 标题行：数据详情 + 排序）
    item {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Colors.gray_F8F9FA)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "数据详情",
                    style = FontMedium(fontSize = 14, color = Colors.black_101828)
                )
                Box {
                    var sortMenuExpanded by remember { mutableStateOf(false) }
                    Icon(
                        painter = painterResource(Res.drawable.icon_menu),
                        contentDescription = "排序菜单",
                        modifier = Modifier
                            .size(16.dp)
                            .click(onClickListener = { sortMenuExpanded = true }),
                        tint = Colors.black_101828
                    )
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        DataCenterSortByOptions.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        DataCenterSortByRadioIcon(selected = value == selectedSortBy)
                                        Text(
                                            text = label,
                                            style = FontRegular(fontSize = 14, color = Colors.black_101828)
                                        )
                                    }
                                },
                                onClick = {
                                    onSortByChange(value)
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // 帖子列表（LazyColumn items）
    itemsIndexed(list) { index, item ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .background(Colors.gray_F8F9FA)
                .then(
                    if (index == list.size - 1) Modifier.clip(
                        RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    ) else Modifier
                )
                .padding(horizontal = 16.dp)
        ) {
            if (index > 0) {
                HorizontalDivider(
                    color = Colors.gray_F3F5F7,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            DataCenterPostItem(
                item = item,
                onItemClick = { onItemClick(item) }
            )
        }
    }

    if (pageStatus == PageStatus.Success && list.isEmpty()) {
        item(key = "data_center_posts_empty") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(Colors.gray_F8F9FA)
                    .padding(vertical = 40.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无数据",
                    style = FontRegular(fontSize = 14, color = Colors.gray_6A7282)
                )
            }
        }
    }

    if (pageStatus == PageStatus.Success && list.isNotEmpty()) {
        if (loadingMorePosts) {
            item(key = "data_center_load_more") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        } else if (!hasMorePosts) {
            item(key = "data_center_list_end") {
                ListEndFooter(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }
        }
    }
}

/**
 * 独立卡片组件，供 Preview 使用。
 */
@Composable
fun DataCenterPostListSection(
    list: List<CreatorStatisticsPostItem>,
    selectedSortBy: Int = 0,
    onSortByChange: (Int) -> Unit = {},
    onItemClick: (CreatorStatisticsPostItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Colors.gray_F8F9FA)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "数据详情",
                style = FontMedium(fontSize = 14, color = Colors.black_101828)
            )
            Icon(
                painter = painterResource(Res.drawable.icon_menu),
                contentDescription = "排序菜单",
                modifier = Modifier.size(16.dp),
                tint = Colors.black_101828
            )
        }
        Column(modifier = Modifier.padding(top = 16.dp)) {
            list.forEachIndexed { index, item ->
                DataCenterPostItem(item = item, onItemClick = { onItemClick(item) })
                if (index < list.size - 1) {
                    HorizontalDivider(
                        color = Colors.gray_F3F5F7,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun DataCenterPostListSectionPreview() {
    val mockList = listOf(
        CreatorStatisticsPostItem(
            postId = 1,
            nickname = "Kaelani Silvermoon",
            avatar = null,
            publishTime = "2025-09-15  09:00:23",
            title = "关于比特币：难忘的瞬间",
            summary = "关于比特币、区块链和加密货币趋势的最新见解。",
            viewCount = 163000,
            likeCount = 163000,
            replyCount = 12000,
            shareCount = 12000,
            revenue = 12000
        )
    )
    DataCenterPostListSection(list = mockList)
}
