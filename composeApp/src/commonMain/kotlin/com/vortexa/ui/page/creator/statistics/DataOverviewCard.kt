package com.vortexa.ui.page.creator.statistics

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vortexa.model.CreatorData
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import org.jetbrains.compose.resources.painterResource
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.arrow_down_full_blue

val DataCenterTimeRangeOptions = listOf(
    1 to "近1日",
    5 to "近5日",
    7 to "近7日",
    15 to "近15日",
    30 to "近1月",
)

@Composable
fun DataOverviewCard(
    data: CreatorData?,
    selectedDays: Int,
    onDaysChange: (Int) -> Unit,
    collapsed: Boolean,
    onCollapsedChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val displayLabel = DataCenterTimeRangeOptions.find { it.first == selectedDays }?.second ?: "近${selectedDays}日"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Colors.gray_F8F9FA)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box {
                Row(
                    modifier = Modifier
                        .clickable { dropdownExpanded = true }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = displayLabel,
                        style = FontMedium(fontSize = 14, color = Colors.blue_277DFF),
                    )
                    Image(
                        painter = painterResource(Res.drawable.arrow_down_full_blue),
                        contentDescription = "选择时间范围",
                        modifier = Modifier.size(14.dp),
                    )
                }
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                ) {
                    DataCenterTimeRangeOptions.forEach { (days, label) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = label,
                                    style = FontRegular(fontSize = 14, color = Colors.black_101828),
                                )
                            },
                            onClick = {
                                onDaysChange(days)
                                dropdownExpanded = false
                            },
                        )
                    }
                }
            }
            Text(
                text = "数据概览",
                style = FontMedium(fontSize = 14, color = Colors.black_101828),
            )
        }

        if (data != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                DataOverviewGridItem(Modifier.weight(1f), "发帖数", data.postCount.toString())
                DataOverviewGridItem(Modifier.weight(1f), "内容浏览", data.viewCount.toString())
                DataOverviewGridItem(Modifier.weight(1f), "点赞", data.likeCount.toString())
                DataOverviewGridItem(Modifier.weight(1f), "评论", data.commentCount.toString())
            }

            if (collapsed) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clickable(onClick = onCollapsedChange),
                    contentAlignment = Alignment.Center,
                ) {
                    HorizontalDivider(
                        color = Colors.gray_f0f4fe,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "点击显示更多",
                        style = FontRegular(fontSize = 12, color = Colors.gray_B1B8C6),
                        modifier = Modifier
                            .background(Colors.gray_F8F9FA)
                            .padding(horizontal = 8.dp),
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    DataOverviewGridItem(Modifier.weight(1f), "涨粉", data.followerGrowth.toString())
                    DataOverviewGridItem(Modifier.weight(1f), "主页访客", data.pageVisitors.toString())
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    HorizontalDivider(
                        color = Colors.gray_f0f4fe,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "收起",
                        style = FontRegular(fontSize = 12, color = Colors.gray_B1B8C6),
                        modifier = Modifier
                            .background(Colors.gray_F8F9FA)
                            .padding(horizontal = 8.dp)
                            .clickable(onClick = onCollapsedChange),
                    )
                }
            }
        } else {
            Text(
                text = "加载中...",
                style = FontRegular(fontSize = 14, color = Colors.gray_6A7282),
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun DataOverviewGridItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = FontRegular(fontSize = 12, color = Colors.gray_6A7282),
        )
        Text(
            text = value,
            style = FontRegular(fontSize = 14, color = Colors.black_101828),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
