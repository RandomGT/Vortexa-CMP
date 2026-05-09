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
import com.vortexa.model.CreatorData
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import vortexa.composeapp.generated.resources.Res

/**
 * 时间范围选项（Figma 504-50864）：近1日、近5日、近7日、近15日、近1月。
 */
val DataCenterTimeRangeOptions = listOf(
    1 to "近1日",
    5 to "近5日",
    7 to "近7日",
    15 to "近15日",
    30 to "近1月"
)

/**
 * 数据概览卡片（Figma 504-51136/504-50887）：近X日下拉选择 + 6 指标网格 + 收起/点击显示更多。
 * 数据来源：GET /v/api/user/creator/data/{days}（[CreatorRepository.getCreatorData]）
 *
 * @param data 创作数据，null 时显示「加载中」
 * @param selectedDays 当前选中的统计天数
 * @param onDaysChange 切换时间范围回调
 * @param collapsed 是否收起，收起时仅显示标题行和收起按钮
 * @param onCollapsedChange 收起/展开切换回调
 */
@Composable
fun DataOverviewCard(
    data: CreatorData?,
    selectedDays: Int,
    onDaysChange: (Int) -> Unit,
    collapsed: Boolean,
    onCollapsedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val displayLabel = DataCenterTimeRangeOptions.find { it.first == selectedDays }?.second ?: "近${selectedDays}日"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Colors.gray_F8F9FA)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 标题行：近X日（可下拉）+ 数据概览
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box {
                Row(
                    modifier = Modifier
                        .clickable { dropdownExpanded = true }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = displayLabel,
                        style = FontMedium(fontSize = 14, color = Colors.blue_277DFF)
                    )
                    Image(
                        painter = painterResource(Res.drawable.arrow_down_full_blue),
                        contentDescription = "选择时间范围",
                        modifier = Modifier.size(14.dp)
                    )
                }
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    DataCenterTimeRangeOptions.forEach { (days, label) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = label,
                                    style = FontRegular(fontSize = 14, color = Colors.black_101828)
                                )
                            },
                            onClick = {
                                onDaysChange(days)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
            Text(
                text = "数据概览",
                style = FontMedium(fontSize = 14, color = Colors.black_101828)
            )
        }

        if (data != null) {
            // 第一行 4 指标（收起/展开态均展示，Figma 504-50887）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                DataOverviewGridItem(Modifier.weight(1f), "发帖数", data.postCount.toString())
                DataOverviewGridItem(Modifier.weight(1f), "内容浏览", data.viewCount.toString())
                DataOverviewGridItem(Modifier.weight(1f), "点赞", data.likeCount.toString())
                DataOverviewGridItem(Modifier.weight(1f), "评论", data.commentCount.toString())
            }

            if (collapsed) {
                // 收起态（Figma 504-50887）：分隔线 +「点击显示更多」
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clickable(onClick = onCollapsedChange),
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalDivider(
                        color = Colors.gray_f0f4fe,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "点击显示更多",
                        style = FontRegular(fontSize = 12, color = Colors.gray_B1B8C6),
                        modifier = Modifier
                            .background(Colors.gray_F8F9FA)
                            .padding(horizontal = 8.dp)
                    )
                }
            } else {
                // 展开态：第二行 4 指标 + 分隔线 +「收起」
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    DataOverviewGridItem(Modifier.weight(1f), "涨粉", data.followerGrowth.toString())
                    DataOverviewGridItem(Modifier.weight(1f), "主页访客", data.pageVisitors.toString())
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalDivider(
                        color = Colors.gray_f0f4fe,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "收起",
                        style = FontRegular(fontSize = 12, color = Colors.gray_B1B8C6),
                        modifier = Modifier
                            .background(Colors.gray_F8F9FA)
                            .padding(horizontal = 8.dp)
                            .clickable(onClick = onCollapsedChange)
                    )
                }
            }
        } else {
            Text(
                text = "加载中...",
                style = FontRegular(fontSize = 14, color = Colors.gray_6A7282),
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun DataOverviewGridItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = FontRegular(fontSize = 12, color = Colors.gray_6A7282)
        )
        Text(
            text = value,
            style = FontRegular(fontSize = 14, color = Colors.black_101828),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
@Preview
private fun DataOverviewCardPreview() {
    val mockData = CreatorData(
        userId = 101,
        days = 7,
        postCount = 1,
        viewCount = 250,
        likeCount = 20,
        commentCount = 33,
        followerGrowth = 1,
        pageVisitors = 250,
        shares = 20,
        revenue = 33
    )
    DataOverviewCard(
        data = mockData,
        selectedDays = 7,
        onDaysChange = {},
        collapsed = false,
        onCollapsedChange = {}
    )
}

@Composable
@Preview
private fun DataOverviewCardCollapsedPreview() {
    val mockData = CreatorData(
        userId = 101,
        days = 7,
        postCount = 1,
        viewCount = 250,
        likeCount = 20,
        commentCount = 33,
        followerGrowth = 1,
        pageVisitors = 250,
        shares = 20,
        revenue = 33
    )
    DataOverviewCard(
        data = mockData,
        selectedDays = 7,
        onDaysChange = {},
        collapsed = true,
        onCollapsedChange = {}
    )
}
