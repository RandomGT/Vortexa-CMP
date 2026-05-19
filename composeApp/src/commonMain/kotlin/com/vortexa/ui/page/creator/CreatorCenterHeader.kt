package com.vortexa.ui.page.creator

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vortexa.ui.component.AvatarImage
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click
import org.jetbrains.compose.resources.painterResource
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.bg_profile
import vortexa.composeapp.generated.resources.icon_back
import vortexa.composeapp.generated.resources.profile_default

data class CreatorCenterHeaderTagItem(
    val text: String,
    val backgroundColor: Color,
)

@Composable
fun CreatorCenterHeader(
    onBackClick: () -> Unit,
    avatarUrl: String? = null,
    nickname: String = "Capper",
    tags: List<CreatorCenterHeaderTagItem>? = null,
    modifier: Modifier = Modifier,
) {
    val effectiveTags = tags.orEmpty()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.bg_profile),
            contentScale = ContentScale.FillBounds,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(24.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                ),
        )

        Icon(
            painter = painterResource(Res.drawable.icon_back),
            contentDescription = "返回",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(18.dp)
                .size(24.dp)
                .click(onClickListener = onBackClick),
        )

        Text(
            text = "创作中心",
            style = FontMedium(fontSize = 18, color = Color.White),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 18.dp),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AvatarImage(
                avatarUrl = avatarUrl,
                contentDescription = null,
                defaultResId = Res.drawable.profile_default,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape),
            )
            Text(
                text = nickname,
                style = FontMedium(fontSize = 18, color = Color.White),
                modifier = Modifier.padding(top = 8.dp),
            )
            if (effectiveTags.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .fillMaxWidth()
                        .clipToBounds(),
                    contentPadding = PaddingValues(horizontal = 40.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = false,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    itemsIndexed(effectiveTags) { _, item ->
                        Text(
                            text = item.text,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            style = FontRegular(fontSize = 12, color = Color.White),
                            modifier = Modifier
                                .background(
                                    color = item.backgroundColor,
                                    shape = RoundedCornerShape(4.dp),
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
