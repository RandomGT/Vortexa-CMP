package com.vortexa.ui.page.teach.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.component.StarRating
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular

/** 评价卡片底部分割线色（Figma W_line_h3 #F3F4F5） */
private val ReviewCardBorder = Color(0xFFF3F4F5)

/**
 * 学员评价列表：从 ViewModel 的 [TeacherProfileViewModel.reviewList] 收集数据并展示卡片列表（Figma 283-30403）。
 * 列表为空时展示通用空页面 [PageStatusView]。
 *
 * @param reviews 评价列表数据
 */
@Composable
fun TeacherProfileReviewList(
    reviews: List<TeacherReviewItem>,
    modifier: Modifier = Modifier
) {
    if (reviews.isEmpty()) {
        PageStatusView(
            status = PageStatus.Empty,
            modifier = modifier.fillMaxSize()
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(reviews, key = { it.id }) { item ->
            TeacherProfileReviewItem(item = item)
        }
    }
}

/**
 * 单条学员评价卡片（Figma 283-30403）：头像、昵称、评分+星级、时间、标题、正文。
 *
 * @param item 单条评价数据
 */
@Composable
internal fun TeacherProfileReviewItem(
    item: TeacherReviewItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 18.dp)
            .clip(RoundedCornerShape(17.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 头像 32dp
            Spacer(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Colors.black_101828)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.userName,
                        style = FontMedium(fontSize = 12, color = Colors.black_101828),
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.offset(x = -3.dp)
                    ) {
                        Text(
                            text = "评分",
                            style = FontRegular(fontSize = 12, color = Colors.gray_6A7282)
                        )
                        StarRating(litCount = item.starCount, starSize = 12.dp)
                    }
                }
                Text(
                    text = item.timeAgo,
                    style = FontRegular(fontSize = 11, color = Colors.gray_6A7282)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = item.title,
                style = FontMedium(fontSize = 16, color = Colors.black_101828)
            )
            Text(
                text = item.content,
                style = FontRegular(fontSize = 15, color = Colors.gray_6A7282)
            )
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ReviewCardBorder)
        )
    }
}

@Preview
@Composable
fun TeacherProfileReviewPreview(){
    TeacherProfileReviewItem(TeacherReviewItem("1", "Leo", "18:33", 3.5f, "太牛逼了", "太牛逼了"))
}
