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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vortexa.model.CreatorStatisticsPostItem
import com.vortexa.ui.component.ListEndFooter
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click
import org.jetbrains.compose.resources.painterResource
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.icon_menu

fun LazyListScope.dataCenterPostListSection(
    selectedSortBy: Int,
    list: List<CreatorStatisticsPostItem>,
    pageStatus: PageStatus,
    hasMorePosts: Boolean,
    loadingMorePosts: Boolean,
    onSortByChange: (Int) -> Unit,
    onItemClick: (CreatorStatisticsPostItem) -> Unit,
) {
    item {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Colors.gray_F8F9FA)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "数据详情",
                    style = FontMedium(fontSize = 14, color = Colors.black_101828),
                )
                Box {
                    var sortMenuExpanded by remember { mutableStateOf(false) }
                    Icon(
                        painter = painterResource(Res.drawable.icon_menu),
                        contentDescription = "排序菜单",
                        modifier = Modifier
                            .size(16.dp)
                            .click(onClickListener = { sortMenuExpanded = true }),
                        tint = Colors.black_101828,
                    )
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false },
                    ) {
                        DataCenterSortByOptions.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        DataCenterSortByRadioIcon(selected = value == selectedSortBy)
                                        Text(
                                            text = label,
                                            style = FontRegular(fontSize = 14, color = Colors.black_101828),
                                        )
                                    }
                                },
                                onClick = {
                                    onSortByChange(value)
                                    sortMenuExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    itemsIndexed(list) { index, item ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .background(Colors.gray_F8F9FA)
                .then(
                    if (index == list.size - 1) {
                        Modifier.clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = 16.dp),
        ) {
            if (index > 0) {
                HorizontalDivider(
                    color = Colors.gray_F3F5F7,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
            DataCenterPostItem(
                item = item,
                onItemClick = { onItemClick(item) },
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
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "暂无数据",
                    style = FontRegular(fontSize = 14, color = Colors.gray_6A7282),
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                }
            }
        } else if (!hasMorePosts) {
            item(key = "data_center_list_end") {
                ListEndFooter(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                )
            }
        }
    }
}
