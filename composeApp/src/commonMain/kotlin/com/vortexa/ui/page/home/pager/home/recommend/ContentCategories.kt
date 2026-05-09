package com.vortexa.ui.page.home.pager.home.recommend

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.DrawableResource
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.img_card_app
import vortexa.composeapp.generated.resources.img_card_crypto
import vortexa.composeapp.generated.resources.img_card_mining
import vortexa.composeapp.generated.resources.img_qa_header


@Composable
fun ContentCategories() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Q&A Header Card
        Box(
            modifier = Modifier
                .height(70.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 4.dp,
                            bottomStart = 4.dp,
                            topEnd = 99.dp,
                            bottomEnd = 99.dp
                        )
                    )
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf( Colors.blue_gradient_end,Colors.blue_gradient_start)
                        )
                    )
            )
            // Text
            Text(
                text = "基础内容 Q&A",
                style = FontMedium(14, Colors.black_101828),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp, bottom = 20.dp)
            )

            // Right Image
            Image(
                painter = painterResource(Res.drawable.img_qa_header),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 70.dp, height = 70.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-0).dp, y = (-10).dp)
                    .rotate(-6.11f),
                contentScale = ContentScale.Fit
            )
        }

        // Three Cards Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y=-22.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card 1
            CategoryCard(
                title = "数字货币\n交易",
                imageRes = Res.drawable.img_card_crypto,
                modifier = Modifier.weight(1f),
                imageModifier = Modifier
                    .size(124.dp)
                    .offset(x = (-30).dp, y = 15.dp)
                    .rotate(-37.12f)
            )

            // Card 2
            CategoryCard(
                title = "矿业类及\n区块链技术",
                imageRes = Res.drawable.img_card_mining,
                modifier = Modifier.weight(1f),
                imageModifier = Modifier
                    .size(126.dp)
                    .offset(x = (-23).dp, y = 16.dp)
                    .rotate(-25.88f)
            )

            // Card 3
            CategoryCard(
                title = "应用\n相关",
                imageRes = Res.drawable.img_card_app,
                modifier = Modifier.weight(1f),
                imageModifier = Modifier
                    .size(155.dp)
                    .offset(x = (-7).dp, y = 20.dp)
            )
        }
    }
}

@Composable
fun CategoryCard(
    title: String,
    imageRes: DrawableResource,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Colors.gray_F8F9FA)
    ) {
        // Image at bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp)) // Clip content to bounds
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = imageModifier.align(Alignment.BottomCenter)
                    .size(120.dp)
                    .offset(x=0.dp,y=40.dp),
                contentScale = ContentScale.Fit
            )
        }

        // Text
        Text(
            text = title,
            style = FontMedium(12, Colors.black_101828),
            modifier = Modifier.padding(start = 12.dp, top = 8.dp)
        )
    }
}
