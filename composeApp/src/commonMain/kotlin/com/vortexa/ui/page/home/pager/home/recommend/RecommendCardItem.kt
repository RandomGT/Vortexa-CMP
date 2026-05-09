package com.vortexa.ui.page.home.pager.home.recommend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vortexa.model.RecommendCard
import com.vortexa.ui.page.teach.profile.TeacherProfileActivity
import com.vortexa.ui.theme.Colors
import com.vortexa.util.extension.click
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.profile_default
import vortexa.composeapp.generated.resources.star_line

/**
 * 标签 Chip（Figma：浅灰底、深灰字、圆角矩形）
 */
@Composable
private fun TagChip(text: String) {
    Surface(
        color = Color(0xFFF3F4F5),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = FontRegular(fontSize = 12, color = Colors.gray_6A7282)
        )
    }
}

/**
 * 推荐卡片单卡（Figma 747-81595 Item）
 * 样式：白底、8dp 圆角、1dp 描边 #F3F4F5；上方 1:1 图 8dp 圆角；标题 14sp Medium、副标题 12sp Regular。
 * @param onCardClick 点击整卡回调，可选，用于跳转导师主页等
 */
@Composable
fun RecommendCardItem(
    card: RecommendCard,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(8.dp)
    val borderColor = Color(0xFFF3F4F5)
    val context: Any? = null
    val clickModifier = Modifier.then(Modifier.click(onClickListener = {
        TeacherProfileActivity.start(context, card.id)
    }))
    Surface(
        modifier = modifier.then(clickModifier),
        shape = cardShape,
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 封面图：使用 avatar 字段（已映射为 imageUrl），有图则加载，无图则占位，尺寸保持 1:1
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Colors.gray_F3F5F7),
                contentAlignment = Alignment.Center
            ) {
                val avatarUrl = card.imageUrl?.takeIf { it.isNotBlank() }
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = card.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(Res.drawable.profile_default),
                        error = painterResource(Res.drawable.profile_default),
                        fallback = painterResource(Res.drawable.profile_default)
                    )
                } else {
                    Text(
                        text = "IMG",
                        style = FontRegular(fontSize = 10, color = Colors.gray_B1B8C6)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = card.title,
                style = FontMedium(fontSize = 14, color = Colors.black_101828),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                card.tags.forEach { tag ->
                    TagChip(text = tag)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${card.price.toInt()} ${card.unit}",
                    style = FontMedium(fontSize = 20, color = Colors.red_FF383C)
                )
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)) {
                    Icon(
                        painter = painterResource(Res.drawable.star_line),
                        contentDescription = null,
                        tint = Colors.gray_B1B8C6,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = card.favorite.toString(),
                        style = FontRegular(fontSize = 12, color = Colors.gray_B1B8C6)
                    )
                }
            }
        }
    }
}

@Composable
fun RecommendCardItemPreview() {
    RecommendCardItem(
        card = RecommendCard(
            id = 1,
            title = "幻夜星辰大海",
            tags = listOf("量化交易", "短线"),
            price = 36.5f,
            unit = "积分",
            favorite = 4.5f
        )
    )
}
