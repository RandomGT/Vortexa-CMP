package com.vortexa.ui.page.home.pager.school

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vortexa.model.SchoolCourseCard
import com.vortexa.ui.component.AvatarImage
import com.vortexa.util.resolveApiMediaUrl
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontBold
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.default_pic
import vortexa.composeapp.generated.resources.star_line

/** 课程卡片圆角：Figma 上 30dp、下 16dp */
private val cardShape = RoundedCornerShape(
    topStart = 30.dp,
    topEnd = 30.dp,
    bottomEnd = 16.dp,
    bottomStart = 16.dp
)

/** 封面区背景色 Figma bg_card #EEF0F1 */
private val coverBg = Color(0xFFEEF0F1)

/**
 * 涡联学院/有声读物课程单卡（Figma 337-45169）
 * 含封面区、标题、讲师+导师角标、购买人数、标签、价格与评分。
 */
@Composable
fun VortexaSchoolCardItem(
    card: SchoolCourseCard,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = cardShape,
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(0.dp)) {
            // 封面区：170:127 比例，8dp 圆角
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(127.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(coverBg),
                contentAlignment = Alignment.Center
            ) {
                val coverPainter = painterResource(Res.drawable.default_pic)
                val coverModel = resolveApiMediaUrl(card.teacherAvatarUrl)
                if (coverModel != null) {
                    AsyncImage(
                        model = coverModel,
                        contentDescription = card.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(127.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = coverPainter,
                        error = coverPainter,
                        fallback = coverPainter
                    )
                } else {
                    Image(
                        painter = coverPainter,
                        contentDescription = card.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(127.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = card.title,
                    style = FontMedium(fontSize = 14, color = Colors.black_101828),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                // 讲师行：头像 + 姓名 + 导师角标
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AvatarImage(
                        avatarUrl = card.teacherAvatarUrl,
                        contentDescription = card.teacherName,
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = card.teacherName,
                        style = FontRegular(fontSize = 12, color = Colors.black_101828),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Colors.blue_277DFF),
                        color = Color.Transparent
                    ) {
                        Text(
                            text = "导师",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = FontRegular(fontSize = 11, color = Colors.blue_277DFF)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "购买人数 ${card.purchaseCount}",
                    style = FontRegular(fontSize = 12, color = Colors.gray_6A7282)
                )
                Spacer(modifier = Modifier.height(8.dp))
                // 标签
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    card.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x146A7282)
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                style = FontRegular(fontSize = 12, color = Colors.gray_6A7282)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // 价格 + 评分
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${card.price} ${card.unit}",
                        style = FontBold(fontSize = 20, color = Color(0xFFFF3A03))
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(Res.drawable.star_line),
                            contentDescription = null,
                            tint = Colors.gray_B1B8C6,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = card.rating.toString(),
                            style = FontMedium(fontSize = 14, color = Colors.gray_B1B8C6)
                        )
                    }
                }
            }
        }
    }
}
