package com.vortexa.ui.page.home.pager.home.communicate

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.model.TeacherItem
import com.vortexa.ui.component.AvatarImage
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.ui.page.teach.profile.TeacherProfileActivity
import com.vortexa.ui.page.teach.schedule.ScheduleActivity
import vortexa.composeapp.generated.resources.Res

/** 技能标签背景色（Figma：rgba(106,114,130,0.08)） */
private val TagBgColor = Color(0x146A7282)

/** 预约按钮背景色（Figma #101828） */
private val ReserveButtonBg = Color(0xFF101828)

/**
 * 单个导师推荐项（Figma 747-82925 导师卡片）：头像、姓名+导师角标、统计数据、技能标签、预约按钮。
 *
 * @param item 导师数据
 * @param onAvatarClick 点击头像回调（进入导师详情等）
 * @param onReserveClick 预约按钮点击回调
 * @param modifier 修饰符
 */
@Composable
private fun TutorRecommendItem(
    item: TeacherItem,
    onAvatarClick: () -> Unit = {},
    onReserveClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(320.dp)
            .padding(12.dp)
    ) {
        // 头像 + 信息 + 预约按钮 行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像 40dp 圆形：优先展示导师头像，缺省时回退默认占位图
            AvatarImage(
                avatarUrl = item.avatar,
                contentDescription = item.nickname,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAvatarClick)
            )
            // 信息区：姓名 + 导师角标 + chevron
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.nickname,
                        style = FontMedium(fontSize = 14, color = Colors.black_101828),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box(
                        modifier = Modifier
                            .border(1.dp, Colors.blue_277DFF, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "导师",
                            style = FontRegular(fontSize = 11, color = Colors.blue_277DFF)
                        )
                    }
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_right_gray),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Colors.gray_6A7282
                    )
                }
                // 统计数据（Mock：粉丝 5K、发帖 20、指导次数 200）
                Text(
                    text = "",
                    style = FontRegular(fontSize = 12, color = Colors.gray_6A7282)
                )
            }
            // 预约按钮 56dp 宽
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(ReserveButtonBg)
                    .clickable { onReserveClick() }
                    .padding(horizontal = 12.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "预约",
                    style = FontMedium(fontSize = 12, color = Color.White)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        // 技能标签行
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (item.tags ?: emptyList()).take(3).forEach { tag ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TagBgColor)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tag,
                        style = FontRegular(fontSize = 12, color = Colors.gray_6A7282)
                    )
                }
            }
        }
    }
}

/**
 * 导师推荐横向滚动区域（Figma 747-82925）：最多展示 6 个导师卡片，支持横向滚动。
 *
 * @param items 导师列表，最多取 6 条
 * @param onReserveClick 预约按钮回调；为 null 时点击「预约」跳转 [ScheduleActivity]（与导师主页一致）
 * @param onAvatarClick 头像点击回调；为 null 时跳转 [TeacherProfileActivity]（与推荐导师卡片一致）
 * @param modifier 修饰符
 */
@Composable
fun TutorRecommendHorizontal(
    items: List<TeacherItem>,
    onReserveClick: ((Long) -> Unit)? = null,
    onAvatarClick: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reserveHandler: (Long) -> Unit = onReserveClick ?: { teacherId ->
        if (teacherId > 0L) {
            Log.d(TAG, "reserve click -> ScheduleActivity, teacherId=$teacherId")
            ScheduleActivity.start(context, teacherId)
        } else {
            Log.w(TAG, "reserve click ignored: invalid teacherId=$teacherId")
        }
    }
    val avatarHandler: (Long) -> Unit = onAvatarClick ?: { teacherId ->
        if (teacherId > 0L) {
            Log.d(TAG, "avatar click -> TeacherProfileActivity, teacherId=$teacherId")
            TeacherProfileActivity.start(context, teacherId)
        } else {
            Log.w(TAG, "avatar click ignored: invalid teacherId=$teacherId")
        }
    }
    val displayList = items.take(6)
    // 每列上下叠两条：按 (0,1)(2,3)… 分组；空列表不产生任何 item，避免访问 displayList[0] 崩溃
    val pairRows = displayList.chunked(2).map { chunk ->
        Pair(chunk.first(), chunk.getOrNull(1))
    }
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp)
    ) {
        itemsIndexed(
            pairRows,
            key = { _, row -> row.first.teacherId }
        ) { _, row ->
            Column {
                TutorRecommendItem(
                    item = row.first,
                    onAvatarClick = { avatarHandler(row.first.teacherId) },
                    onReserveClick = { reserveHandler(row.first.teacherId) }
                )
                row.second?.let { second ->
                    TutorRecommendItem(
                        item = second,
                        onAvatarClick = { avatarHandler(second.teacherId) },
                        onReserveClick = { reserveHandler(second.teacherId) }
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun TutorRecommendHorizontalPreview() {
    val previewItems = listOf(
        TeacherItem(1L, null, "导师 A", listOf("量化", "短线"), 36.5f, "4.5"),
        TeacherItem(2L, null, "导师 B", listOf("趋势"), 28f, "4.8")
    )
    TutorRecommendHorizontal(
        items = previewItems,
        onReserveClick = { _ -> },
        onAvatarClick = { _ -> }
    )
}

private const val TAG = "TutorRecommendHorizontal"
